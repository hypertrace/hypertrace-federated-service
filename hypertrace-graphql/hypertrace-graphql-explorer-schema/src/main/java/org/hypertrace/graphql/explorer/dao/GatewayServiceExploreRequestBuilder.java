package org.hypertrace.graphql.explorer.dao;

import static io.reactivex.rxjava3.core.Single.zip;

import com.google.common.collect.Sets;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.common.TimeAggregation;
import org.hypertrace.graphql.explorer.request.ExploreRequest;
import org.hypertrace.graphql.explorer.schema.argument.IntervalArgument;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

public class GatewayServiceExploreRequestBuilder {
  private final Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter;
  private final Converter<
          List<AttributeAssociation<AggregatableOrderArgument>>, List<OrderByExpression>>
      orderConverter;
  private final Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter;
  private final Converter<Collection<MetricAggregationRequest>, Set<Expression>>
      aggregationConverter;
  private final Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>> seriesConverter;

  @Inject
  GatewayServiceExploreRequestBuilder(
      Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter,
      Converter<List<AttributeAssociation<AggregatableOrderArgument>>, List<OrderByExpression>>
          orderConverter,
      Converter<Collection<AttributeRequest>, Set<Expression>> attributeConverter,
      Converter<Collection<MetricAggregationRequest>, Set<Expression>> aggregationConverter,
      Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>> seriesConverter) {
    this.filterConverter = filterConverter;
    this.orderConverter = orderConverter;
    this.attributeConverter = attributeConverter;
    this.aggregationConverter = aggregationConverter;
    this.seriesConverter = seriesConverter;
  }

  Single<org.hypertrace.gateway.service.v1.explore.ExploreRequest> buildRequest(
      ExploreRequest request) {
    return zip(
        this.attributeConverter.convert(
            Sets.difference(request.attributeRequests(), request.groupByAttributeRequests())),
        this.orderConverter.convert(request.orderArguments()),
        this.attributeConverter.convert(request.groupByAttributeRequests()),
        this.filterConverter.convert(request.filterArguments()),
        this.buildAnyAggregations(request),
        this.buildAnyTimeAggregations(request),
        (attributes, orderBys, groupBys, filter, aggregations, series) ->
            org.hypertrace.gateway.service.v1.explore.ExploreRequest.newBuilder()
                .setContext(request.scope())
                .setStartTimeMillis(request.timeRange().startTime().toEpochMilli())
                .setEndTimeMillis(request.timeRange().endTime().toEpochMilli())
                .addAllSelection(attributes)
                .addAllSelection(aggregations)
                .addAllTimeAggregation(series)
                .addAllOrderBy(orderBys)
                .addAllGroupBy(groupBys)
                .setLimit(request.limit())
                .setIncludeRestGroup(request.includeRest())
                .setOffset(request.offset())
                .setFilter(filter)
                .setSpaceId(request.spaceId().orElse("")) // String proto default value
                .build());
  }

  private Single<Set<Expression>> buildAnyAggregations(ExploreRequest exploreRequest) {
    if (exploreRequest.timeInterval().isPresent()) {
      return Single.just(Collections.emptySet());
    }

    return this.aggregationConverter.convert(exploreRequest.aggregationRequests());
  }

  private Single<Set<TimeAggregation>> buildAnyTimeAggregations(ExploreRequest exploreRequest) {
    if (exploreRequest.timeInterval().isEmpty()) {
      return Single.just(Collections.emptySet());
    }
    IntervalArgument intervalArgument = exploreRequest.timeInterval().orElseThrow();
    return Observable.fromIterable(exploreRequest.aggregationRequests())
        .map(
            aggregation ->
                new ExplorerMetricSeriesRequest(
                    aggregation, intervalArgument.size(), intervalArgument.units()))
        .cast(MetricSeriesRequest.class)
        .collect(Collectors.toUnmodifiableSet())
        .flatMap(this.seriesConverter::convert);
  }

  @Value
  @Accessors(fluent = true)
  private static class ExplorerMetricSeriesRequest implements MetricSeriesRequest {
    MetricAggregationRequest aggregationRequest;
    int size;
    TimeUnit unit;

    @Override
    public String alias() {
      // Different alias to match aggregations
      return this.aggregationRequest.alias();
    }

    public Duration period() {
      return Duration.of(size, unit.getChronoUnit());
    }
  }
}
