package org.hypertrace.graphql.entity;

import io.reactivex.rxjava3.core.Observable;
import javax.inject.Inject;
import org.hypertrace.core.graphql.attributes.IdMapping;
import org.hypertrace.core.graphql.attributes.IdMappingLoader;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.entity.type.service.rxclient.EntityTypeClient;
import org.hypertrace.graphql.config.HypertraceGraphQlServiceConfig;

class EntityIdMappingLoader implements IdMappingLoader {
  private final EntityTypeClient entityTypeClient;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;

  @Inject
  EntityIdMappingLoader(
      GrpcChannelRegistry channelRegistry,
      HypertraceGraphQlServiceConfig graphQlServiceConfig,
      GraphQlGrpcContextBuilder grpcContextBuilder) {
    this.grpcContextBuilder = grpcContextBuilder;
    this.entityTypeClient =
        EntityTypeClient.builder(
                channelRegistry.forAddress(
                    graphQlServiceConfig.getEntityServiceHost(),
                    graphQlServiceConfig.getEntityServicePort()))
            .build();
  }

  @Override
  public Observable<IdMapping> loadMappings(GraphQlRequestContext requestContext) {
    return this.grpcContextBuilder
        .build(requestContext)
        .callInContext(this.entityTypeClient::getAll)
        .map(
            entityType ->
                IdMapping.forId(entityType.getAttributeScope(), entityType.getIdAttributeKey()));
  }
}
