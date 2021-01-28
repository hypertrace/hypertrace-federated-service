package org.hypertrace.graphql.entity.dao;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.grpc.CallCredentials;
import io.reactivex.rxjava3.core.Single;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.gateway.service.GatewayServiceGrpc;
import org.hypertrace.gateway.service.GatewayServiceGrpc.GatewayServiceFutureStub;
import org.hypertrace.gateway.service.v1.entity.EntitiesRequest;
import org.hypertrace.gateway.service.v1.entity.EntitiesResponse;
import org.hypertrace.graphql.entity.health.BaselineDao;
import org.hypertrace.graphql.entity.request.EntityRequest;
import org.hypertrace.graphql.entity.schema.EntityResultSet;

@Singleton
class GatewayServiceEntityDao implements EntityDao {
  private static final int DEFAULT_DEADLINE_SEC = 10;
  private final GatewayServiceFutureStub gatewayServiceStub;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;
  private final GatewayServiceEntityRequestBuilder requestBuilder;
  private final GatewayServiceEntityConverter entityConverter;
  private final BaselineDao baselineDao;

  @Inject
  GatewayServiceEntityDao(
      GraphQlServiceConfig serviceConfig,
      CallCredentials credentials,
      GraphQlGrpcContextBuilder grpcContextBuilder,
      GrpcChannelRegistry grpcChannelRegistry,
      GatewayServiceEntityRequestBuilder requestBuilder,
      GatewayServiceEntityConverter entityConverter,
      BaselineDao baselineDao) {
    this.grpcContextBuilder = grpcContextBuilder;
    this.requestBuilder = requestBuilder;
    this.entityConverter = entityConverter;
    this.baselineDao = baselineDao;

    this.gatewayServiceStub =
        GatewayServiceGrpc.newFutureStub(
                grpcChannelRegistry.forAddress(
                    serviceConfig.getGatewayServiceHost(), serviceConfig.getGatewayServicePort()))
            .withCallCredentials(credentials);
  }

  @Override
  public Single<EntityResultSet> getEntities(EntityRequest request) {
    return this.requestBuilder
        .buildRequest(request)
        .flatMap(
            serverRequest ->
                this.makeRequest(request.resultSetRequest().context(), serverRequest)
                    .flatMap(
                        serverResponse ->
                            baselineDao
                                .getBaselines(
                                    request.resultSetRequest().context(),
                                    serverRequest,
                                    serverResponse,
                                    request)
                                .flatMap(
                                    baselineResponse ->
                                        this.entityConverter.convert(
                                            request, serverResponse, baselineResponse))));
  }

  private Single<EntitiesResponse> makeRequest(
      GraphQlRequestContext context, EntitiesRequest request) {
    return Single.fromFuture(
        this.grpcContextBuilder
            .build(context)
            .callInContext(
                () ->
                    this.gatewayServiceStub
                        .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                        .getEntities(request)));
  }
}
