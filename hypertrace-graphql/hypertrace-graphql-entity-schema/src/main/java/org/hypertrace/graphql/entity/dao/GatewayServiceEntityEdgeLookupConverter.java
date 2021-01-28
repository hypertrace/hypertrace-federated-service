package org.hypertrace.graphql.entity.dao;

import static io.reactivex.rxjava3.core.Single.zip;

import com.google.common.collect.Table;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Value;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.gateway.service.v1.entity.EntitiesResponse;
import org.hypertrace.gateway.service.v1.entity.Entity;
import org.hypertrace.gateway.service.v1.entity.EntityInteraction;
import org.hypertrace.graphql.entity.dao.GatewayServiceEntityEdgeLookupConverter.EdgeLookup;
import org.hypertrace.graphql.entity.request.EntityRequest;
import org.hypertrace.graphql.entity.schema.EdgeResultSet;
import org.hypertrace.graphql.entity.schema.EntityType;

class GatewayServiceEntityEdgeLookupConverter
    implements BiConverter<EntityRequest, EntitiesResponse, EdgeLookup> {
  private final GatewayServiceEntityEdgeTableConverter interactionsMapConverter;

  @Inject
  GatewayServiceEntityEdgeLookupConverter(
      GatewayServiceEntityEdgeTableConverter interactionsMapConverter) {
    this.interactionsMapConverter = interactionsMapConverter;
  }

  @Override
  public Single<EdgeLookup> convert(EntityRequest request, EntitiesResponse response) {
    return zip(
        this.interactionsMapConverter.convert(
            request.incomingEdgeRequests(),
            this.collectInteractions(response, Entity::getIncomingInteractionList)),
        this.interactionsMapConverter.convert(
            request.outgoingEdgeRequests(),
            this.collectInteractions(response, Entity::getOutgoingInteractionList)),
        EdgeLookup::new);
  }

  private Set<InteractionResponse> collectInteractions(
      EntitiesResponse response, Function<Entity, List<EntityInteraction>> interactionMapper) {
    return response.getEntityList().stream()
        .flatMap(entity -> this.streamInteractionsForEntity(entity, interactionMapper))
        .collect(Collectors.toUnmodifiableSet());
  }

  private Stream<InteractionResponse> streamInteractionsForEntity(
      Entity entity, Function<Entity, List<EntityInteraction>> interactionMapper) {
    return interactionMapper.apply(entity).stream()
        .map(interaction -> new InteractionResponse(entity, interaction));
  }

  @Value
  static class EdgeLookup {
    Table<Entity, String, EdgeResultSet> incoming;
    Table<Entity, String, EdgeResultSet> outgoing;
  }
}
