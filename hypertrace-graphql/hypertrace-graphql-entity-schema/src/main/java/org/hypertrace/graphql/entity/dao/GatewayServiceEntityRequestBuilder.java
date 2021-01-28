package org.hypertrace.graphql.entity.dao;

import static io.reactivex.rxjava3.core.Single.zip;
import static org.hypertrace.core.graphql.common.utils.CollectorUtils.flatten;

import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.common.TimeAggregation;
import org.hypertrace.gateway.service.v1.entity.EntitiesRequest;
import org.hypertrace.graphql.entity.request.EntityRequest;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

class GatewayServiceEntityRequestBuilder {

  private final Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter;
  private final Converter<
          List<AttributeAssociation<AggregatableOrderArgument>>, List<OrderByExpression>>
      orderConverter;
  private final Converter<Collection<AttributeRequest>, Set<Expression>> selectionConverter;
  private final Converter<Collection<MetricAggregationRequest>, Set<Expression>>
      aggregationConverter;
  private final Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>> seriesConverter;
  private final GatewayServiceEntityInteractionRequestBuilder interactionRequestBuilder;

  @Inject
  GatewayServiceEntityRequestBuilder(
      Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter,
      Converter<List<AttributeAssociation<AggregatableOrderArgument>>, List<OrderByExpression>>
          orderConverter,
      Converter<Collection<AttributeRequest>, Set<Expression>> selectionConverter,
      Converter<Collection<MetricAggregationRequest>, Set<Expression>> aggregationConverter,
      Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>> seriesConverter,
      GatewayServiceEntityInteractionRequestBuilder interactionRequestBuilder) {
    this.filterConverter = filterConverter;
    this.orderConverter = orderConverter;
    this.selectionConverter = selectionConverter;
    this.aggregationConverter = aggregationConverter;
    this.seriesConverter = seriesConverter;
    this.interactionRequestBuilder = interactionRequestBuilder;
  }

  Single<EntitiesRequest> buildRequest(EntityRequest entityRequest) {
    ResultSetRequest<AggregatableOrderArgument> resultSetRequest = entityRequest.resultSetRequest();
    return zip(
        this.selectionConverter.convert(resultSetRequest.attributes()),
        this.orderConverter.convert(resultSetRequest.orderArguments()),
        this.filterConverter.convert(resultSetRequest.filterArguments()),
        this.aggregationConverter.convert(
            entityRequest.metricRequests().stream()
                .collect(flatten(MetricRequest::aggregationRequests))),
        this.seriesConverter.convert(
            entityRequest.metricRequests().stream()
                .collect(flatten(MetricRequest::seriesRequests))),
        this.interactionRequestBuilder.build(entityRequest.incomingEdgeRequests()),
        this.interactionRequestBuilder.build(entityRequest.outgoingEdgeRequests()),
        (selections,
            orderBys,
            filter,
            aggregations,
            series,
            incomingInteractions,
            outgoingInteractions) ->
            EntitiesRequest.newBuilder()
                .setEntityType(entityRequest.entityType())
                .setStartTimeMillis(resultSetRequest.timeRange().startTime().toEpochMilli())
                .setEndTimeMillis(resultSetRequest.timeRange().endTime().toEpochMilli())
                .addAllSelection(selections)
                .addAllSelection(aggregations)
                .addAllTimeAggregation(series)
                .setIncomingInteractions(incomingInteractions)
                .setOutgoingInteractions(outgoingInteractions)
                .addAllOrderBy(orderBys)
                .setLimit(resultSetRequest.limit())
                .setOffset(resultSetRequest.offset())
                .setFilter(filter)
                .setIncludeNonLiveEntities(entityRequest.includeInactive())
                .setSpaceId(resultSetRequest.spaceId().orElse("")) // String proto default value\
                .setFetchTotal(entityRequest.fetchTotal())
                .build());
  }
}
