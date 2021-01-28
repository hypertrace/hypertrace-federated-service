package org.hypertrace.graphql.entity.request;

import static io.reactivex.rxjava3.core.Single.zip;
import static org.hypertrace.graphql.entity.schema.Edge.EDGE_NEIGHBOR_KEY;
import static org.hypertrace.graphql.entity.schema.Entity.ENTITY_INCOMING_EDGES_KEY;
import static org.hypertrace.graphql.entity.schema.Entity.ENTITY_OUTGOING_EDGES_KEY;

import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterOperatorType;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterType;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.request.MetricRequestBuilder;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

class NeighborEntitiesRequestBuilder {

  private static final SelectionQuery INCOMING_EDGE_QUERY =
      SelectionQuery.namedChild(ENTITY_INCOMING_EDGES_KEY);
  private static final SelectionQuery OUTGOING_EDGE_QUERY =
      SelectionQuery.namedChild(ENTITY_OUTGOING_EDGES_KEY);
  private static final int NEIGHBOR_QUERY_OFFSET = 0;
  private final GraphQlSelectionFinder selectionFinder;
  private final ResultSetRequestBuilder resultSetRequestBuilder;
  private final MetricRequestBuilder metricRequestBuilder;
  private final EdgeRequestBuilder edgeRequestBuilder;
  private final FilterRequestBuilder filterRequestBuilder;

  @Inject
  NeighborEntitiesRequestBuilder(
      GraphQlSelectionFinder selectionFinder,
      ResultSetRequestBuilder resultSetRequestBuilder,
      MetricRequestBuilder metricRequestBuilder,
      EdgeRequestBuilder edgeRequestBuilder,
      FilterRequestBuilder filterRequestBuilder) {
    this.selectionFinder = selectionFinder;
    this.resultSetRequestBuilder = resultSetRequestBuilder;
    this.metricRequestBuilder = metricRequestBuilder;
    this.edgeRequestBuilder = edgeRequestBuilder;
    this.filterRequestBuilder = filterRequestBuilder;
  }

  Single<EntityRequest> buildNeighborRequest(
      GraphQlRequestContext context,
      String entityScope,
      TimeRangeArgument timeRange,
      Optional<String> space,
      Collection<String> neighborIds,
      Collection<SelectedField> edgeFields) {

    return this.build(
        context, entityScope, timeRange, space, neighborIds, this.getNeighborFields(edgeFields));
  }

  private Single<EntityRequest> build(
      GraphQlRequestContext context,
      String entityScope,
      TimeRangeArgument timeRange,
      Optional<String> space,
      Collection<String> neighborIds,
      Collection<SelectedField> neighborFields) {
    return zip(
        this.buildResultSetRequest(
            context, timeRange, space, entityScope, neighborIds, neighborFields),
        this.metricRequestBuilder.build(context, entityScope, neighborFields.stream()),
        this.edgeRequestBuilder.buildIncomingEdgeRequest(
            context, timeRange, space, this.getIncomingEdges(neighborFields)),
        this.edgeRequestBuilder.buildOutgoingEdgeRequest(
            context, timeRange, space, this.getOutgoingEdges(neighborFields)),
        (resultSetRequest, metricRequestList, incomingEdges, outgoingEdges) ->
            new NeighborEntityRequest(
                entityScope,
                resultSetRequest,
                metricRequestList,
                incomingEdges,
                outgoingEdges,
                // entity interactions doesn't support time agnostic nature, and would mean
                // that the neighbors would have to be live in the requested time range.
                // Supporting time agnostic interations would mean a change in the way interactions
                // are implemented
                false,
                false));
  }

  private Single<ResultSetRequest<AggregatableOrderArgument>> buildResultSetRequest(
      GraphQlRequestContext context,
      TimeRangeArgument timeRange,
      Optional<String> space,
      String entityScope,
      Collection<String> neighborIds,
      Collection<SelectedField> neighborFields) {
    return this.getIdFilter(context, entityScope, neighborIds)
        .flatMap(
            filters ->
                this.resultSetRequestBuilder.build(
                    context,
                    entityScope,
                    neighborIds.size(),
                    NEIGHBOR_QUERY_OFFSET,
                    timeRange,
                    Collections.emptyList(),
                    filters,
                    neighborFields.stream(),
                    space));
  }

  private Single<List<AttributeAssociation<FilterArgument>>> getIdFilter(
      GraphQlRequestContext context, String neighborScope, Collection<String> neighborIds) {
    return this.filterRequestBuilder.build(
        context, neighborScope, Set.of(new EntityNeighborIdFilter(neighborIds, neighborScope)));
  }

  private Stream<SelectedField> getIncomingEdges(Collection<SelectedField> neighborFields) {
    return neighborFields.stream()
        .map(SelectedField::getSelectionSet)
        .flatMap(
            neighborSelections ->
                this.selectionFinder.findSelections(neighborSelections, INCOMING_EDGE_QUERY));
  }

  private Stream<SelectedField> getOutgoingEdges(Collection<SelectedField> neighborFields) {
    return neighborFields.stream()
        .map(SelectedField::getSelectionSet)
        .flatMap(
            neighborSelections ->
                this.selectionFinder.findSelections(neighborSelections, OUTGOING_EDGE_QUERY));
  }

  private Collection<SelectedField> getNeighborFields(Collection<SelectedField> edgeFields) {
    return edgeFields.stream()
        .flatMap(this::getNeighborFields)
        .collect(Collectors.toUnmodifiableSet());
  }

  private Stream<SelectedField> getNeighborFields(SelectedField edgeField) {
    return this.selectionFinder.findSelections(
        edgeField.getSelectionSet(), SelectionQuery.namedChild(EDGE_NEIGHBOR_KEY));
  }

  @Value
  @Accessors(fluent = true)
  private static class EntityNeighborIdFilter implements FilterArgument {
    FilterType type = FilterType.ID;
    String key = null;
    FilterOperatorType operator = FilterOperatorType.IN;
    Collection<String> value;
    AttributeScope idType = null;
    String idScope;
  }

  @Value
  @Accessors(fluent = true)
  private static class NeighborEntityRequest implements EntityRequest {
    String entityType;
    ResultSetRequest<AggregatableOrderArgument> resultSetRequest;
    List<MetricRequest> metricRequests;
    EdgeSetGroupRequest incomingEdgeRequests;
    EdgeSetGroupRequest outgoingEdgeRequests;
    boolean includeInactive;
    boolean fetchTotal;
  }
}
