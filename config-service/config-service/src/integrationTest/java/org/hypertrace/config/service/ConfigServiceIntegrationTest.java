package org.hypertrace.config.service;

import static org.hypertrace.config.service.IntegrationTestUtils.getConfigValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.Value;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.hypertrace.config.service.v1.ConfigServiceGrpc;
import org.hypertrace.config.service.v1.ContextSpecificConfig;
import org.hypertrace.config.service.v1.DeleteConfigRequest;
import org.hypertrace.config.service.v1.DeleteConfigResponse;
import org.hypertrace.config.service.v1.GetAllConfigsRequest;
import org.hypertrace.config.service.v1.GetAllConfigsResponse;
import org.hypertrace.config.service.v1.GetConfigRequest;
import org.hypertrace.config.service.v1.GetConfigResponse;
import org.hypertrace.config.service.v1.UpsertConfigRequest;
import org.hypertrace.config.service.v1.UpsertConfigResponse;
import org.hypertrace.core.grpcutils.client.GrpcClientRequestContextUtil;
import org.hypertrace.core.grpcutils.client.RequestContextClientCallCredsProviderFactory;
import org.hypertrace.core.serviceframework.IntegrationTestServerUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for ConfigService
 */
public class ConfigServiceIntegrationTest {

  private static final String RESOURCE_NAME = "foo";
  private static final String RESOURCE_NAMESPACE = "bar";
  private static final String TENANT_1 = "tenant1";
  private static final String TENANT_2 = "tenant2";
  private static final String CONTEXT_1 = "ctx1";
  private static final String CONTEXT_2 = "ctx2";
  private static final String DEFAULT_CONTEXT = "DEFAULT-CONTEXT";

  private static ConfigServiceGrpc.ConfigServiceBlockingStub configServiceBlockingStub;
  private static Value config1;
  private static Value config2;
  private static Value config3;

  @BeforeAll
  public static void setup() throws IOException {
    System.out.println("Starting Config Service E2E Test");
    IntegrationTestServerUtil.startServices(new String[]{"config-service"});

    Channel channel = ManagedChannelBuilder.forAddress("localhost", 50101)
        .usePlaintext()
        .build();

    configServiceBlockingStub = ConfigServiceGrpc.newBlockingStub(channel)
        .withCallCredentials(
            RequestContextClientCallCredsProviderFactory.getClientCallCredsProvider().get());

    config1 = getConfigValue("config1.yaml");
    config2 = getConfigValue("config2.yaml");
    config3 = getConfigValue("config3.yaml");
  }

  @AfterAll
  public static void teardown() {
    IntegrationTestServerUtil.shutdownServices();
  }

  @BeforeEach
  public void clearPreviousState() {
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, DEFAULT_CONTEXT);
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_2, DEFAULT_CONTEXT);
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, CONTEXT_1);
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_2, CONTEXT_1);
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, CONTEXT_2);
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_2, CONTEXT_2);
  }

  @Test
  public void testUpsertConfig() {
    // upsert first version of config for tenant-1
    UpsertConfigResponse upsertConfigResponse = upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE,
        TENANT_1, Optional.empty(), config1);
    assertEquals(config1, upsertConfigResponse.getConfig());

    // upsert second version of config for tenant-1
    upsertConfigResponse = upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1,
        Optional.empty(), config2);
    assertEquals(config3, upsertConfigResponse.getConfig());

    // test across tenants - upsert first version of config for tenant-2
    upsertConfigResponse = upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_2,
        Optional.empty(), config1);
    assertEquals(config1, upsertConfigResponse.getConfig());
  }

  @Test
  public void testGetConfig() {
    // upsert config with default or no context
    upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, Optional.empty(), config1);

    // upsert config with context
    upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, Optional.of(CONTEXT_1), config2);

    // get default config (without context)
    GetConfigResponse getDefaultConfigResponse =
        getConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1);
    assertEquals(config1, getDefaultConfigResponse.getConfig());

    // get config with context as CONTEXT_1 - should merge the first two configs(which is config3)
    GetConfigResponse getContext1ConfigResponse =
        getConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, CONTEXT_1);
    assertEquals(config3, getContext1ConfigResponse.getConfig());

    // get config with context as CONTEXT_2 - should return the config with default context
    GetConfigResponse getContext2ConfigResponse =
        getConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, CONTEXT_2);
    assertEquals(config1, getContext2ConfigResponse.getConfig());
  }

  @Test
  public void testGetAllConfigs() {
    // upsert config with default or no context
    upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, Optional.empty(), config1);

    // upsert config with context
    upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, Optional.of(CONTEXT_1), config2);

    List<ContextSpecificConfig> contextSpecificConfigList = getAllConfigs(RESOURCE_NAME,
        RESOURCE_NAMESPACE, TENANT_1).getContextSpecificConfigsList();
    assertEquals(2, contextSpecificConfigList.size());
    assertEquals(
        ContextSpecificConfig.newBuilder().setContext(DEFAULT_CONTEXT).setConfig(config1).build(),
        contextSpecificConfigList.get(0));
    assertEquals(
        ContextSpecificConfig.newBuilder().setContext(CONTEXT_1).setConfig(config2).build(),
        contextSpecificConfigList.get(1));
  }

  @Test
  public void testDeleteConfig() {
    // upsert config with default or no context
    upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, Optional.empty(), config1);

    // upsert config with context
    upsertConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, Optional.of(CONTEXT_1), config2);

    // delete config with context
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, CONTEXT_1);

    // get config with context should return default config
    Value config = getConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, CONTEXT_1).getConfig();
    assertEquals(config1, config);

    // delete config with default context also
    deleteConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, DEFAULT_CONTEXT);

    // get config with context should return empty config
    StatusRuntimeException exception =
        assertThrows(
            StatusRuntimeException.class,
            () -> getConfig(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_1, CONTEXT_1));

    assertEquals(Status.NOT_FOUND, exception.getStatus());
  }

  private UpsertConfigResponse upsertConfig(String resourceName, String resourceNamespace,
      String tenantId, Optional<String> context, Value config) {
    UpsertConfigRequest.Builder builder = UpsertConfigRequest.newBuilder()
        .setResourceName(resourceName)
        .setResourceNamespace(resourceNamespace)
        .setConfig(config);
    if (context.isPresent()) {
      builder.setContext(context.get());
    }
    return GrpcClientRequestContextUtil.executeInTenantContext(
        tenantId, () -> configServiceBlockingStub.upsertConfig(builder.build()));
  }

  private GetConfigResponse getConfig(String resourceName, String resourceNamespace,
      String tenantId, String... contexts) {
    GetConfigRequest getConfigRequest = GetConfigRequest.newBuilder()
        .setResourceName(resourceName)
        .setResourceNamespace(resourceNamespace)
        .addAllContexts(Arrays.asList(contexts))
        .build();
    return GrpcClientRequestContextUtil.executeInTenantContext(
        tenantId, () -> configServiceBlockingStub.getConfig(getConfigRequest));
  }

  private GetAllConfigsResponse getAllConfigs(String resourceName, String resourceNamespace,
      String tenantId) {
    GetAllConfigsRequest request = GetAllConfigsRequest.newBuilder()
        .setResourceName(resourceName)
        .setResourceNamespace(resourceNamespace)
        .build();
    return GrpcClientRequestContextUtil.executeInTenantContext(
        tenantId, () -> configServiceBlockingStub.getAllConfigs(request));
  }

  private DeleteConfigResponse deleteConfig(String resourceName, String resourceNamespace,
      String tenantId, String context) {
    DeleteConfigRequest request = DeleteConfigRequest.newBuilder()
        .setResourceName(resourceName)
        .setResourceNamespace(resourceNamespace)
        .setContext(context)
        .build();
    return GrpcClientRequestContextUtil.executeInTenantContext(
        tenantId, () -> configServiceBlockingStub.deleteConfig(request));
  }

}
