package org.hypertrace.core.bootstrapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Context;
import io.grpc.ManagedChannelBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.hypertrace.core.attribute.service.client.AttributeServiceClient;
import org.hypertrace.core.attribute.service.client.config.AttributeServiceClientConfig;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeMetadataFilter;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.documentstore.Datastore;
import org.hypertrace.core.documentstore.DatastoreProvider;
import org.hypertrace.core.documentstore.DocumentStoreConfig;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.hypertrace.core.grpcutils.context.RequestContextConstants;
import org.hypertrace.entity.service.client.config.EntityServiceClientConfig;
import org.hypertrace.entity.type.service.client.EntityTypeServiceClient;
import org.hypertrace.entity.type.service.v1.EntityRelationshipTypeFilter;
import org.hypertrace.entity.type.service.v1.EntityTypeFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hypertrace.core.bootstrapper.dao.ConfigBootstrapStatusDao.CONFIG_BOOTSTRAPPER_COLLECTION;

public class ConfigBootstrapperIntegrationTest {

  private static final String TENANT_ID = "__default";
  private static EntityTypeServiceClient entityTypeServiceClient;
  private static org.hypertrace.entity.type.client.EntityTypeServiceClient entityTypeServiceClientV2;
  private static AttributeServiceClient attributesServiceClient;

  @BeforeAll
  public static void setUp() {
    Config config = ConfigFactory.parseResources("config-bootstrapper-test/application.conf");

    EntityServiceClientConfig esConfig = EntityServiceClientConfig.from(config);
    Channel channel =
        ClientInterceptors.intercept(
            ManagedChannelBuilder.forAddress(esConfig.getHost(), esConfig.getPort())
                .usePlaintext()
                .build(),
            new TenantIdClientInterceptor(TENANT_ID));
    entityTypeServiceClient = new EntityTypeServiceClient(channel);
    entityTypeServiceClientV2 = new org.hypertrace.entity.type.client.EntityTypeServiceClient(channel);

    AttributeServiceClientConfig asConfig = AttributeServiceClientConfig.from(config);
    channel =
        ClientInterceptors.intercept(
            ManagedChannelBuilder.forAddress(asConfig.getHost(), asConfig.getPort())
                .usePlaintext()
                .build(),
            new TenantIdClientInterceptor(TENANT_ID));
    attributesServiceClient = new AttributeServiceClient(channel);

    String dataStoreType = config.getString(DocumentStoreConfig.DATASTORE_TYPE_CONFIG_KEY);
    Datastore datastore =
        DatastoreProvider.getDatastore(dataStoreType, config.getConfig(dataStoreType));
    datastore.getCollection(CONFIG_BOOTSTRAPPER_COLLECTION).drop();
  }

  @BeforeEach
  public void setupMethod() {
    entityTypeServiceClient.deleteEntityTypes(TENANT_ID, EntityTypeFilter.newBuilder().build());
    entityTypeServiceClient.deleteEntityRelationshipTypes(
        TENANT_ID, EntityRelationshipTypeFilter.newBuilder().build());
    entityTypeServiceClientV2.deleteAllEntityTypes(TENANT_ID);
    attributesServiceClient.delete(TENANT_ID, AttributeMetadataFilter.newBuilder().build());
  }

  @Test
  public void testConfigBootstrapperValidateUpgradeAndRollback() {
    String resourcesPath =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource("config-bootstrapper-test")
            .getPath();

    // Since the clients to run Config commands are created internal to this code,
    // we need to set the tenantId in the context so that it's propagated.
    RequestContext requestContext = new RequestContext();
    requestContext.add(RequestContextConstants.TENANT_ID_HEADER_KEY, TENANT_ID);
    Context context = Context.current().withValue(RequestContext.CURRENT, requestContext);
    context.run(
        () ->
            ConfigBootstrapper.main(
                new String[] {
                  "-c",
                  resourcesPath + "/application.conf",
                  "-C",
                  resourcesPath,
                  "--validate",
                  "--upgrade"
                }));

    // Assert attribute is created
    Iterator<AttributeMetadata> attributeMetadataIterator =
        attributesServiceClient.findAttributes(
            TENANT_ID,
            AttributeMetadataFilter.newBuilder()
                .addScope(AttributeScope.TRACE)
                .addKey("id")
                .build());
    List<AttributeMetadata> attributeMetadataList =
        StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(attributeMetadataIterator, 0), false)
            .collect(Collectors.toList());
    Assertions.assertEquals(1, attributeMetadataList.size());

    // Assert entity type and relationship type are created
    Assertions.assertEquals(2, entityTypeServiceClient.getAllEntityTypes(TENANT_ID).size());
    Assertions.assertEquals(
        1, entityTypeServiceClient.getAllEntityRelationshipTypes(TENANT_ID).size());

    Assertions.assertEquals(3, entityTypeServiceClientV2.getAllEntityTypes(TENANT_ID).size());
    Assertions.assertEquals(1, entityTypeServiceClientV2.queryEntityTypes(TENANT_ID,
        List.of("API")).size());

    // Rollback to version 1
    // Since the clients to run Config commands are created internal to this code,
    // we need to set the tenantId in the context so that it's propagated.
    context.run(
        () ->
            ConfigBootstrapper.main(
                new String[] {
                  "-c", resourcesPath + "/application.conf", "-C", resourcesPath, "--rollback", "1"
                }));
    // Assert entity type and relationship type are created
    Assertions.assertEquals(1, entityTypeServiceClient.getAllEntityTypes(TENANT_ID).size());
  }
}
