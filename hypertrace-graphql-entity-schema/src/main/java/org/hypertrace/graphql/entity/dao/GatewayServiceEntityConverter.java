package org.hypertrace.graphql.entity.dao;

import static io.reactivex.rxjava3.core.Single.zip;
import static org.hypertrace.graphql.entity.dao.GatewayServiceEntityEdgeFetcher.EMPTY_EDGE_RESULT_SET;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.entity.EntitiesResponse;
import org.hypertrace.graphql.entity.dao.GatewayServiceEntityEdgeLookupConverter.EdgeLookup;
import org.hypertrace.graphql.entity.request.EntityRequest;
import org.hypertrace.graphql.entity.schema.EdgeResultSet;
import org.hypertrace.graphql.entity.schema.Entity;
import org.hypertrace.graphql.entity.schema.EntityResultSet;
import org.hypertrace.graphql.entity.schema.EntityType;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.schema.MetricContainer;

class GatewayServiceEntityConverter {
  private final BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
      attributeMapConverter;

  private final BiConverter<
          Collection<MetricRequest>,
          org.hypertrace.gateway.service.v1.entity.Entity,
          Map<String, MetricContainer>>
      metricContainerConverter;
  private final GatewayServiceEntityEdgeLookupConverter edgeLookupConverter;

  @Inject
  GatewayServiceEntityConverter(
      BiConverter<Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>
          attributeMapConverter,
      BiConverter<
              Collection<MetricRequest>,
              org.hypertrace.gateway.service.v1.entity.Entity,
              Map<String, MetricContainer>>
          metricContainerConverter,
      GatewayServiceEntityEdgeLookupConverter edgeLookupConverter) {
    this.attributeMapConverter = attributeMapConverter;
    this.metricContainerConverter = metricContainerConverter;
    this.edgeLookupConverter = edgeLookupConverter;
  }

  Single<EntityResultSet> convert(EntityRequest request, EntitiesResponse response) {
    return this.edgeLookupConverter
        .convert(request, response)
        .flatMap(edgeLookup -> this.convert(request, response, edgeLookup));
  }

  private Single<EntityResultSet> convert(
      EntityRequest request, EntitiesResponse response, EdgeLookup edgeLookup) {

    return Observable.fromIterable(response.getEntityList())
        .flatMapSingle(
            entity ->
                this.convertEntity(
                    request,
                    entity,
                    edgeLookup.getIncoming().row(entity),
                    edgeLookup.getOutgoing().row(entity)))
        .toList()
        .map(
            entities ->
                new ConvertedEntityResultSet(entities, response.getTotal(), entities.size()));
  }

  private Single<Entity> convertEntity(
      EntityRequest entityRequest,
      org.hypertrace.gateway.service.v1.entity.Entity platformEntity,
      Map<EntityType, EdgeResultSet> incomingEdges,
      Map<EntityType, EdgeResultSet> outgoingEdges) {
    return zip(
        this.attributeMapConverter.convert(
            entityRequest.resultSetRequest().attributes(), platformEntity.getAttributeMap()),
        this.metricContainerConverter.convert(entityRequest.metricRequests(), platformEntity),
        (attrMap, containerMap) ->
            new ConvertedEntity(
                attrMap
                    .get(entityRequest.resultSetRequest().idAttribute().attribute().key())
                    .toString(),
                entityRequest.entityType(),
                attrMap,
                containerMap,
                incomingEdges,
                outgoingEdges));
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedEntity implements Entity {
    String id;
    EntityType type;
    Map<String, Object> attributeValues;
    Map<String, MetricContainer> metricContainers;
    Map<EntityType, EdgeResultSet> incomingEdges;
    Map<EntityType, EdgeResultSet> outgoingEdges;

    @Override
    public Object attribute(String key) {
      return this.attributeValues.get(key);
    }

    @Override
    public MetricContainer metric(String key) {
      return this.metricContainers.get(key);
    }

    @Override
    public EdgeResultSet incomingEdges(EntityType neighborType) {
      return this.incomingEdges.getOrDefault(neighborType, EMPTY_EDGE_RESULT_SET);
    }

    @Override
    public EdgeResultSet outgoingEdges(EntityType neighborType) {
      return this.outgoingEdges.getOrDefault(neighborType, EMPTY_EDGE_RESULT_SET);
    }
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedEntityResultSet implements EntityResultSet {
    List<Entity> results;
    long total;
    long count;
  }
}
