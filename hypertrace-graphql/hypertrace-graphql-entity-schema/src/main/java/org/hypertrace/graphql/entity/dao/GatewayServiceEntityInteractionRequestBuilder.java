package org.hypertrace.graphql.entity.dao;

import static io.reactivex.rxjava3.core.Single.zip;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterOperatorType;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterType;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.entity.InteractionsRequest;
import org.hypertrace.graphql.entity.request.EdgeSetGroupRequest;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;

class GatewayServiceEntityInteractionRequestBuilder {
  private static final Integer DEFAULT_INTERACTION_LIMIT = 1000;
  private final Converter<Collection<AttributeRequest>, Set<Expression>> selectionConverter;
  private final Converter<Collection<MetricAggregationRequest>, Set<Expression>>
      aggregationConverter;
  private final Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter;

  @Inject
  GatewayServiceEntityInteractionRequestBuilder(
      Converter<Collection<AttributeRequest>, Set<Expression>> selectionConverter,
      Converter<Collection<MetricAggregationRequest>, Set<Expression>> aggregationConverter,
      Converter<Collection<AttributeAssociation<FilterArgument>>, Filter> filterConverter) {
    this.selectionConverter = selectionConverter;
    this.aggregationConverter = aggregationConverter;
    this.filterConverter = filterConverter;
  }

  Single<InteractionsRequest> build(EdgeSetGroupRequest edgeSetRequestGroup) {
    if (edgeSetRequestGroup.entityTypes().isEmpty()) {
      return Single.just(InteractionsRequest.getDefaultInstance());
    }

    return zip(
        this.collectSelectionsAndAggregations(edgeSetRequestGroup),
        this.buildEntityTypeFilter(edgeSetRequestGroup),
        (selections, filter) ->
            InteractionsRequest.newBuilder()
                .addAllSelection(selections)
                .setFilter(filter)
                .setLimit(DEFAULT_INTERACTION_LIMIT)
                .build());
  }

  private Single<Set<Expression>> collectSelectionsAndAggregations(EdgeSetGroupRequest request) {
    return this.selectionConverter
        .convert(request.attributeRequests())
        .mergeWith(this.aggregationConverter.convert(request.metricAggregationRequests()))
        .toObservable()
        .flatMap(Observable::fromIterable)
        .collect(Collectors.toUnmodifiableSet());
  }

  private Single<Filter> buildEntityTypeFilter(EdgeSetGroupRequest request) {
    return Observable.fromIterable(request.entityTypes())
        .collect(Collectors.toUnmodifiableSet())
        .map(
            entityTypes ->
                AttributeAssociation.<FilterArgument>of(
                    request.neighborTypeAttribute().attribute(),
                    new EntityNeighborTypeFilter(
                        request.neighborTypeAttribute().attribute().key(), entityTypes)))
        .flatMap(filterAssociation -> this.filterConverter.convert(Set.of(filterAssociation)));
  }

  @Value
  @Accessors(fluent = true)
  private static class EntityNeighborTypeFilter implements FilterArgument {
    FilterType type = FilterType.ATTRIBUTE;
    String key;
    FilterOperatorType operator = FilterOperatorType.IN;
    Collection<String> value;
    AttributeScope idType = null;
    String idScope = null;
  }
}
