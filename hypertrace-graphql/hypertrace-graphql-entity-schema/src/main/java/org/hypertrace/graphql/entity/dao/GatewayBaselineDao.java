package org.hypertrace.graphql.entity.dao;

import io.grpc.CallCredentials;
import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.gateway.service.GatewayServiceGrpc;
import org.hypertrace.gateway.service.v1.baseline.BaselineEntitiesRequest;
import org.hypertrace.gateway.service.v1.baseline.BaselineEntitiesResponse;
import org.hypertrace.gateway.service.v1.baseline.BaselineTimeAggregation;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.FunctionExpression;
import org.hypertrace.gateway.service.v1.common.TimeAggregation;
import org.hypertrace.gateway.service.v1.entity.EntitiesRequest;
import org.hypertrace.gateway.service.v1.entity.EntitiesResponse;
import org.hypertrace.gateway.service.v1.entity.Entity;
import org.hypertrace.graphql.entity.health.BaselineDao;
import org.hypertrace.graphql.entity.request.EntityRequest;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import static io.reactivex.rxjava3.core.Single.zip;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hypertrace.core.graphql.common.utils.CollectorUtils.flatten;

@Singleton
class GatewayBaselineDao implements BaselineDao {
  private static final int DEFAULT_DEADLINE_SEC = 10;
  private final GatewayServiceGrpc.GatewayServiceFutureStub gatewayServiceStub;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;
  private final Converter<Collection<MetricAggregationRequest>, Set<Expression>>
      aggregationConverter;
  private final Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>> seriesConverter;

  @Inject
  GatewayBaselineDao(
      GrpcChannelRegistry grpcChannelRegistry,
      GraphQlServiceConfig serviceConfig,
      CallCredentials credentials,
      GraphQlGrpcContextBuilder grpcContextBuilder,
      Converter<Collection<MetricAggregationRequest>, Set<Expression>> aggregationConverter,
      Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>> seriesConverter) {
    this.grpcContextBuilder = grpcContextBuilder;
    this.gatewayServiceStub =
        GatewayServiceGrpc.newFutureStub(
                grpcChannelRegistry.forAddress(
                    serviceConfig.getGatewayServiceHost(), serviceConfig.getGatewayServicePort()))
            .withCallCredentials(credentials);
    this.seriesConverter = seriesConverter;
    this.aggregationConverter = aggregationConverter;
  }

  @Override
  public Single<BaselineEntitiesResponse> getBaselines(
      GraphQlRequestContext context,
      EntitiesRequest entitiesRequest,
      EntitiesResponse entitiesResponse,
      EntityRequest request) {
    return this.buildRequest(entitiesRequest, entitiesResponse, request)
        .flatMap(baselineEntitiesRequest -> makeRequest(context, baselineEntitiesRequest));
  }

  private Single<BaselineEntitiesRequest> buildRequest(
      EntitiesRequest entitiesRequest,
      EntitiesResponse entitiesResponse,
      EntityRequest entityRequest) {
    return zip(
        this.aggregationConverter.convert(
            entityRequest.metricRequests().stream()
                .map(MetricRequest::aggregationRequests)
                .flatMap(Collection::stream)
                .filter(MetricAggregationRequest::baseline)
                .collect(Collectors.toUnmodifiableList())),
        this.seriesConverter.convert(
            entityRequest.metricRequests().stream()
                .collect(flatten(MetricRequest::baselineSeriesRequests))),
        (aggregations, seriesRequests) ->
            BaselineEntitiesRequest.newBuilder()
                .setStartTimeMillis(entitiesRequest.getStartTimeMillis())
                .setEndTimeMillis(entitiesRequest.getEndTimeMillis())
                .addAllEntityIds(
                    entitiesResponse.getEntityList().stream()
                        .map(Entity::getId)
                        .collect(Collectors.toList()))
                .setEntityType(entitiesRequest.getEntityType())
                .addAllBaselineAggregateRequest(getBaselineAggregateRequests(aggregations))
                .addAllBaselineMetricSeriesRequest(getBaselineTimeSeriesRequests(seriesRequests))
                .build());
  }

  private Set<BaselineTimeAggregation> getBaselineTimeSeriesRequests(
      Set<TimeAggregation> seriesRequests) {
    return seriesRequests.stream()
        .map(
            seriesRequest ->
                BaselineTimeAggregation.newBuilder()
                    .setAggregation(seriesRequest.getAggregation().getFunction())
                    .setPeriod(seriesRequest.getPeriod())
                    .build())
        .collect(Collectors.toUnmodifiableSet());
  }

  private Set<FunctionExpression> getBaselineAggregateRequests(Set<Expression> aggregations) {
    return aggregations.stream()
        .map(Expression::getFunction)
        .collect(Collectors.toUnmodifiableSet());
  }

  private Single<BaselineEntitiesResponse> makeRequest(
      GraphQlRequestContext context, BaselineEntitiesRequest request) {
    if (request.getBaselineAggregateRequestCount() == 0
        && request.getBaselineMetricSeriesRequestCount() == 0) {
      return Single.just(BaselineEntitiesResponse.getDefaultInstance());
    }
    return Single.fromFuture(
        this.grpcContextBuilder
            .build(context)
            .callInContext(
                () ->
                    this.gatewayServiceStub
                        .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                        .getBaselineForEntities(request)));
  }
}
