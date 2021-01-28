package org.hypertrace.graphql.entity.dao;

import static io.reactivex.rxjava3.core.Single.zip;
import static java.util.Objects.isNull;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.gateway.service.v1.common.AggregatedMetricValue;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.entity.EntityInteraction;
import org.hypertrace.graphql.entity.request.EdgeSetGroupRequest;
import org.hypertrace.graphql.entity.schema.Edge;
import org.hypertrace.graphql.entity.schema.EdgeResultSet;
import org.hypertrace.graphql.entity.schema.Entity;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.schema.BaselinedMetricAggregationContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GatewayServiceEntityEdgeFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(GatewayServiceEntityEdgeFetcher.class);
  static final EdgeResultSet EMPTY_EDGE_RESULT_SET = new ConvertedEdgeResultSet(List.of());

  private final EntityNeighborMapFetcher neighborMapFetcher;

  private final BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
      attributeMapConverter;
  private final BiConverter<
          Collection<MetricAggregationRequest>,
          Map<String, AggregatedMetricValue>,
          Map<String, BaselinedMetricAggregationContainer>>
          baselineMetricAggregationContainerMapConverter;

  @Inject
  GatewayServiceEntityEdgeFetcher(
      EntityNeighborMapFetcher neighborMapFetcher,
      BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
          attributeMapConverter,
      BiConverter<
              Collection<MetricAggregationRequest>,
              Map<String, AggregatedMetricValue>,
              Map<String, BaselinedMetricAggregationContainer>>
              baselineMetricAggregationContainerMapConverter) {
    this.neighborMapFetcher = neighborMapFetcher;
    this.attributeMapConverter = attributeMapConverter;
    this.baselineMetricAggregationContainerMapConverter = baselineMetricAggregationContainerMapConverter;
  }

  Single<Map<org.hypertrace.gateway.service.v1.entity.Entity, EdgeResultSet>> fetchForEntityType(
      String entityType,
      EdgeSetGroupRequest edgeSetGroupRequest,
      Collection<InteractionResponse> interactions) {
    return this.neighborMapFetcher
        .fetchNeighbors(edgeSetGroupRequest, entityType, interactions)
        .flatMapObservable(
            neighborMap ->
                this.buildEdgesForEachEntity(edgeSetGroupRequest, interactions, neighborMap))
        .collect(CollectorUtils.immutableMapEntryCollector());
  }

  private Observable<Entry<org.hypertrace.gateway.service.v1.entity.Entity, EdgeResultSet>>
      buildEdgesForEachEntity(
          EdgeSetGroupRequest edgeSetGroupRequest,
          Collection<InteractionResponse> interactionResponses,
          Map<InteractionResponse, Entity> neighborMap) {
    return Observable.fromIterable(interactionResponses)
        .groupBy(InteractionResponse::getSource)
        .flatMapSingle(
            response ->
                this.buildEdgesForEntity(
                    response.getKey(), response, edgeSetGroupRequest, neighborMap));
  }

  private Single<Entry<org.hypertrace.gateway.service.v1.entity.Entity, EdgeResultSet>>
      buildEdgesForEntity(
          org.hypertrace.gateway.service.v1.entity.Entity key,
          Observable<InteractionResponse> interactions,
          EdgeSetGroupRequest edgeSetGroupRequest,
          Map<InteractionResponse, Entity> neighborMap) {
    return interactions
        .flatMapMaybe(
            response ->
                this.buildEdge(
                    edgeSetGroupRequest, response.getInteraction(), key, neighborMap.get(response)))
        .collect(Collectors.toUnmodifiableList())
        .map(ConvertedEdgeResultSet::new)
        .map(edgeResultSet -> Map.entry(key, edgeResultSet));
  }

  private Maybe<Edge> buildEdge(
      EdgeSetGroupRequest edgeSetGroupRequest,
      EntityInteraction response,
      org.hypertrace.gateway.service.v1.entity.Entity source,
      Entity neighbor) {
    if (isNull(neighbor)) {
      // This should not happen, but we're being defensive due to some underlying issues with entity
      // and interaction query mismatches
      LOG.error(
          "Response missing neighbor entity for interaction {} from source entity {}",
          response,
          source);
      return Maybe.empty();
    }

    return zip(
            this.attributeMapConverter.convert(
                edgeSetGroupRequest.attributeRequests(), response.getAttributeMap()),
            this.baselineMetricAggregationContainerMapConverter.convert(
                edgeSetGroupRequest.metricAggregationRequests(), response.getMetricsMap()),
            (attributes, metrics) -> (Edge) new ConvertedEdge(neighbor, attributes, metrics))
        .toMaybe();
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedEdge implements Edge {
    Entity neighbor;
    Map<String, Object> attributeValues;
    Map<String, BaselinedMetricAggregationContainer> metricContainers;

    @Override
    public Object attribute(String key) {
      return this.attributeValues.get(key);
    }

    @Override
    public BaselinedMetricAggregationContainer metric(String key) {
      return this.metricContainers.get(key);
    }
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedEdgeResultSet implements EdgeResultSet {
    List<Edge> results;

    @Override
    public long count() {
      return this.results.size();
    }

    @Override
    public long total() {
      //  Total not currently exposed by gateway
      return this.results.size();
    }
  }
}
