package org.hypertrace.graphql.explorer.request;

import static io.reactivex.rxjava3.core.Single.zip;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import graphql.schema.DataFetchingFieldSelectionSet;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.AttributeRequestBuilder;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.page.LimitArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.page.OffsetArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.space.SpaceArgument;
import org.hypertrace.core.graphql.common.utils.attributes.AttributeAssociator;
import org.hypertrace.core.graphql.common.utils.attributes.AttributeScopeStringTranslator;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContext;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContextArgument;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerScopeArgument;
import org.hypertrace.graphql.explorer.schema.argument.GroupByArgument;
import org.hypertrace.graphql.explorer.schema.argument.IntervalArgument;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

class DefaultExploreRequestBuilder implements ExploreRequestBuilder {
  private static final int DEFAULT_LIMIT = 100;
  private static final int DEFAULT_OFFSET = 0;

  private final AttributeRequestBuilder attributeRequestBuilder;
  private final ArgumentDeserializer argumentDeserializer;
  private final AttributeAssociator attributeAssociator;
  private final ExploreSelectionRequestBuilder selectionRequestBuilder;
  private final FilterRequestBuilder filterRequestBuilder;
  private final AttributeScopeStringTranslator scopeStringTranslator;

  @Inject
  DefaultExploreRequestBuilder(
      AttributeRequestBuilder attributeRequestBuilder,
      ArgumentDeserializer argumentDeserializer,
      AttributeAssociator attributeAssociator,
      ExploreSelectionRequestBuilder selectionRequestBuilder,
      FilterRequestBuilder filterRequestBuilder,
      AttributeScopeStringTranslator scopeStringTranslator) {
    this.attributeRequestBuilder = attributeRequestBuilder;
    this.argumentDeserializer = argumentDeserializer;
    this.attributeAssociator = attributeAssociator;
    this.selectionRequestBuilder = selectionRequestBuilder;
    this.filterRequestBuilder = filterRequestBuilder;
    this.scopeStringTranslator = scopeStringTranslator;
  }

  @Override
  public Single<ExploreRequest> build(
      GraphQlRequestContext requestContext,
      Map<String, Object> arguments,
      DataFetchingFieldSelectionSet selectionSet) {

    String explorerScope =
        this.argumentDeserializer
            .deserializePrimitive(arguments, ExplorerContextArgument.class)
            .map(ExplorerContext::getScopeString)
            .or(
                () ->
                    this.argumentDeserializer.deserializePrimitive(
                        arguments, ExplorerScopeArgument.class))
            .map(this.scopeStringTranslator::fromExternal)
            .orElseThrow();

    return this.build(requestContext, explorerScope, arguments, selectionSet);
  }

  private Single<ExploreRequest> build(
      GraphQlRequestContext requestContext,
      String explorerScope,
      Map<String, Object> arguments,
      DataFetchingFieldSelectionSet selectionSet) {

    int limit =
        this.argumentDeserializer
            .deserializePrimitive(arguments, LimitArgument.class)
            .orElse(DEFAULT_LIMIT);

    int offset =
        this.argumentDeserializer
            .deserializePrimitive(arguments, OffsetArgument.class)
            .orElse(DEFAULT_OFFSET);

    TimeRangeArgument timeRange =
        this.argumentDeserializer
            .deserializeObject(arguments, TimeRangeArgument.class)
            .orElseThrow();

    List<AggregatableOrderArgument> requestedOrders =
        this.argumentDeserializer
            .deserializeObjectList(arguments, AggregatableOrderArgument.class)
            .orElse(emptyList());

    List<FilterArgument> requestedFilters =
        this.argumentDeserializer
            .deserializeObjectList(arguments, FilterArgument.class)
            .orElse(emptyList());

    Optional<GroupByArgument> groupBy =
        this.argumentDeserializer.deserializeObject(arguments, GroupByArgument.class);

    Optional<String> spaceId =
        this.argumentDeserializer.deserializePrimitive(arguments, SpaceArgument.class);

    Set<String> groupByKeys =
        groupBy.map(GroupByArgument::keys).map(Set::copyOf).orElse(emptySet());

    Optional<IntervalArgument> intervalArgument =
        this.argumentDeserializer.deserializeObject(arguments, IntervalArgument.class);

    Single<Set<AttributeRequest>> attributeSelections =
        this.selectionRequestBuilder.getAttributeSelections(
            requestContext, explorerScope, selectionSet);

    Single<Set<MetricAggregationRequest>> aggregationSelections =
        this.selectionRequestBuilder.getAggregationSelections(
            requestContext, explorerScope, selectionSet);

    Single<List<AttributeAssociation<AggregatableOrderArgument>>> orderSingle =
        this.attributeAssociator
            .associateAttributes(requestContext, explorerScope, requestedOrders, OrderArgument::key)
            .collect(Collectors.toUnmodifiableList());

    Single<List<AttributeAssociation<FilterArgument>>> filterSingle =
        this.filterRequestBuilder.build(requestContext, explorerScope, requestedFilters);

    return zip(
        attributeSelections,
        aggregationSelections,
        orderSingle,
        filterSingle,
        this.buildGroupByAttributes(requestContext, explorerScope, groupByKeys),
        (attributes, aggregations, orders, filters, groupByAttribute) ->
            new DefaultExploreRequest(
                requestContext,
                explorerScope,
                timeRange,
                limit,
                offset,
                attributes,
                aggregations,
                orders,
                filters,
                groupByAttribute,
                intervalArgument,
                groupBy.map(GroupByArgument::includeRest).orElse(false),
                spaceId));
  }

  private Single<Set<AttributeRequest>> buildGroupByAttributes(
      GraphQlRequestContext context, String explorerScope, Set<String> groupByKeys) {
    return Observable.fromIterable(groupByKeys)
        .flatMapSingle(key -> this.attributeRequestBuilder.buildForKey(context, explorerScope, key))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultExploreRequest implements ExploreRequest {
    GraphQlRequestContext requestContext;
    String scope;
    TimeRangeArgument timeRange;
    int limit;
    int offset;
    Set<AttributeRequest> attributeRequests;
    Set<MetricAggregationRequest> aggregationRequests;
    List<AttributeAssociation<AggregatableOrderArgument>> orderArguments;
    List<AttributeAssociation<FilterArgument>> filterArguments;
    Set<AttributeRequest> groupByAttributeRequests;
    Optional<IntervalArgument> timeInterval;
    boolean includeRest;
    Optional<String> spaceId;
  }
}
