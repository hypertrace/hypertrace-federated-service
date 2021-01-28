package org.hypertrace.graphql.explorer.dao;

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
import org.hypertrace.gateway.service.v1.explore.ExploreResponse;
import org.hypertrace.graphql.explorer.request.ExploreRequest;
import org.hypertrace.graphql.explorer.schema.ExploreResultSet;

@Singleton
class GatewayServiceExplorerDao implements ExplorerDao {
  private static final int DEFAULT_DEADLINE_SEC = 10;
  private final GatewayServiceFutureStub gatewayServiceStub;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;
  private final GatewayServiceExploreRequestBuilder requestBuilder;
  private final GatewayServiceExploreResponseConverter responseConverter;

  @Inject
  GatewayServiceExplorerDao(
      GraphQlServiceConfig serviceConfig,
      CallCredentials credentials,
      GraphQlGrpcContextBuilder grpcContextBuilder,
      GrpcChannelRegistry grpcChannelRegistry,
      GatewayServiceExploreRequestBuilder requestBuilder,
      GatewayServiceExploreResponseConverter responseConverter) {
    this.grpcContextBuilder = grpcContextBuilder;
    this.requestBuilder = requestBuilder;
    this.responseConverter = responseConverter;

    this.gatewayServiceStub =
        GatewayServiceGrpc.newFutureStub(
                grpcChannelRegistry.forAddress(
                    serviceConfig.getGatewayServiceHost(), serviceConfig.getGatewayServicePort()))
            .withCallCredentials(credentials);
  }

  @Override
  public Single<ExploreResultSet> explore(ExploreRequest request) {
    return this.requestBuilder
        .buildRequest(request)
        .flatMap(serverRequest -> this.makeRequest(request.requestContext(), serverRequest))
        .flatMap(serverResponse -> this.responseConverter.convert(request, serverResponse));
  }

  private Single<ExploreResponse> makeRequest(
      GraphQlRequestContext context,
      org.hypertrace.gateway.service.v1.explore.ExploreRequest request) {
    return Single.fromFuture(
        this.grpcContextBuilder
            .build(context)
            .callInContext(
                () ->
                    this.gatewayServiceStub
                        .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                        .explore(request)));
  }
}
