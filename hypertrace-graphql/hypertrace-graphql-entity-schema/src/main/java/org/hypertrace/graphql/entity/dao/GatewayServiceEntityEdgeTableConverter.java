package org.hypertrace.graphql.entity.dao;

import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static com.google.common.collect.Tables.immutableCell;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table.Cell;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.entity.Entity;
import org.hypertrace.graphql.entity.request.EdgeSetGroupRequest;
import org.hypertrace.graphql.entity.schema.EdgeResultSet;

class GatewayServiceEntityEdgeTableConverter
    implements BiConverter<
        EdgeSetGroupRequest,
        Collection<InteractionResponse>,
        ImmutableTable<Entity, String, EdgeResultSet>> {

  private final Converter<Value, Object> valueConverter;
  private final GatewayServiceEntityEdgeFetcher edgeFetcher;

  @Inject
  GatewayServiceEntityEdgeTableConverter(
      Converter<Value, Object> valueConverter, GatewayServiceEntityEdgeFetcher edgeFetcher) {
    this.valueConverter = valueConverter;
    this.edgeFetcher = edgeFetcher;
  }

  @Override
  public Single<ImmutableTable<Entity, String, EdgeResultSet>> convert(
      EdgeSetGroupRequest request, Collection<InteractionResponse> responses) {

    return this.groupInteractionsByNeighborType(responses, request.neighborTypeAttribute())
        .flattenAsObservable(Map::entrySet)
        .flatMap(entry -> this.flattenEdgeCells(request, entry.getKey(), entry.getValue()))
        .collect(toImmutableTable(Cell::getRowKey, Cell::getColumnKey, Cell::getValue));
  }

  private Single<Map<String, Collection<InteractionResponse>>> groupInteractionsByNeighborType(
      Collection<InteractionResponse> interactions, AttributeRequest neighborType) {

    return Observable.fromIterable(interactions)
        .flatMapSingle(entry -> this.builtInteractionTypeEntry(entry, neighborType))
        .collect(ImmutableSetMultimap.toImmutableSetMultimap(Entry::getKey, Entry::getValue))
        .map(Multimap::asMap);
  }

  private Single<Entry<String, InteractionResponse>> builtInteractionTypeEntry(
      InteractionResponse response, AttributeRequest neighborType) {

    return this.getEntityType(response.getInteraction().getAttributeMap().get(neighborType.alias()))
        .map(entityType -> Map.entry(entityType, response));
  }

  private Observable<Cell<Entity, String, EdgeResultSet>> flattenEdgeCells(
      EdgeSetGroupRequest request,
      String entityType,
      Collection<InteractionResponse> interactions) {
    return this.edgeFetcher
        .fetchForEntityType(entityType, request, interactions)
        .flattenAsObservable(Map::entrySet)
        .map(
            entityEdgeResultSetEntry ->
                immutableCell(
                    entityEdgeResultSetEntry.getKey(),
                    entityType,
                    entityEdgeResultSetEntry.getValue()));
  }

  private Single<String> getEntityType(Value entityTypeValue) {
    return this.valueConverter.convert(entityTypeValue).map(String::valueOf);
  }
}
