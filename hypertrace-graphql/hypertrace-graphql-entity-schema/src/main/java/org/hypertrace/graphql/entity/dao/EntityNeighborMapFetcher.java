package org.hypertrace.graphql.entity.dao;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.rx.BoundedIoScheduler;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.graphql.entity.request.EdgeSetGroupRequest;
import org.hypertrace.graphql.entity.request.EntityRequest;
import org.hypertrace.graphql.entity.schema.Entity;
import org.hypertrace.graphql.entity.schema.EntityResultSet;

class EntityNeighborMapFetcher {

  private final EntityDao entityDao;
  private final Converter<Value, Object> valueConverter;
  private final Scheduler boundedIoScheduler;

  @Inject
  EntityNeighborMapFetcher(
      EntityDao entityDao,
      Converter<Value, Object> valueConverter,
      @BoundedIoScheduler Scheduler boundedIoScheduler) {
    this.entityDao = entityDao;
    this.valueConverter = valueConverter;
    this.boundedIoScheduler = boundedIoScheduler;
  }

  Maybe<Map<InteractionResponse, Entity>> fetchNeighbors(
      EdgeSetGroupRequest edgeRequest,
      String entityType,
      Collection<InteractionResponse> edgeResponses) {
    return this.mapInteractionsByNeighborId(edgeRequest.neighborIdAttribute(), edgeResponses)
        .filter(map -> map.size() > 0)
        .flatMapSingle(
            interactionsByNeighborId ->
                this.fetchNeighbors(
                    interactionsByNeighborId,
                    edgeRequest.buildNeighborRequest(
                        entityType, interactionsByNeighborId.keySet())));
  }

  private Single<Map<InteractionResponse, Entity>> fetchNeighbors(
      Multimap<String, InteractionResponse> interactionsByNeighborId,
      Single<EntityRequest> neighborRequestSingle) {
    return neighborRequestSingle
        .observeOn(this.boundedIoScheduler)
        .flatMap(this.entityDao::getEntities)
        .flattenAsObservable(EntityResultSet::results)
        .flatMapStream(
            entity -> this.streamNeighborEntries(interactionsByNeighborId.get(entity.id()), entity))
        .collect(CollectorUtils.immutableMapEntryCollector());
  }

  private Single<ImmutableSetMultimap<String, InteractionResponse>> mapInteractionsByNeighborId(
      AttributeRequest neighborIdAttribute, Collection<InteractionResponse> edgeResponses) {
    return Observable.fromIterable(edgeResponses)
        .flatMapSingle(response -> this.buildInteractionEntry(neighborIdAttribute, response))
        .collect(ImmutableSetMultimap.toImmutableSetMultimap(Entry::getKey, Entry::getValue));
  }

  private Single<Entry<String, InteractionResponse>> buildInteractionEntry(
      AttributeRequest neighborIdAttribute, InteractionResponse response) {
    return this.valueConverter
        .convert(response.getInteraction().getAttributeMap().get(neighborIdAttribute.alias()))
        .map(String::valueOf)
        .map(id -> Map.entry(id, response));
  }

  private Stream<Entry<InteractionResponse, Entity>> streamNeighborEntries(
      Collection<InteractionResponse> interactions, Entity entity) {
    return interactions.stream().map(interaction -> Map.entry(interaction, entity));
  }
}
