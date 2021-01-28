package org.hypertrace.graphql.entity.request;

import static io.reactivex.rxjava3.core.Single.zip;
import static org.hypertrace.graphql.entity.schema.Entity.ENTITY_INCOMING_EDGES_KEY;
import static org.hypertrace.graphql.entity.schema.Entity.ENTITY_OUTGOING_EDGES_KEY;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.results.ResultSet;
import org.hypertrace.core.graphql.common.schema.results.arguments.space.SpaceArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;
import org.hypertrace.graphql.entity.schema.EntityType;
import org.hypertrace.graphql.entity.schema.argument.EntityScopeArgument;
import org.hypertrace.graphql.entity.schema.argument.EntityTypeArgument;
import org.hypertrace.graphql.entity.schema.argument.IncludeInactiveArgument;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.request.MetricRequestBuilder;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

class DefaultEntityRequestBuilder implements EntityRequestBuilder {

  private final ResultSetRequestBuilder resultSetRequestBuilder;
  private final MetricRequestBuilder metricRequestBuilder;
  private final ArgumentDeserializer argumentDeserializer;
  private final GraphQlSelectionFinder selectionFinder;
  private final EdgeRequestBuilder edgeRequestBuilder;

  @Inject
  DefaultEntityRequestBuilder(
      ResultSetRequestBuilder resultSetRequestBuilder,
      MetricRequestBuilder metricRequestBuilder,
      ArgumentDeserializer argumentDeserializer,
      GraphQlSelectionFinder selectionFinder,
      EdgeRequestBuilder edgeRequestBuilder) {
    this.resultSetRequestBuilder = resultSetRequestBuilder;
    this.metricRequestBuilder = metricRequestBuilder;
    this.argumentDeserializer = argumentDeserializer;
    this.selectionFinder = selectionFinder;
    this.edgeRequestBuilder = edgeRequestBuilder;
  }

  @Override
  public Single<EntityRequest> build(
      GraphQlRequestContext context,
      Map<String, Object> arguments,
      DataFetchingFieldSelectionSet selectionSet) {
    String entityScope =
        this.argumentDeserializer
            .deserializePrimitive(arguments, EntityTypeArgument.class)
            .map(EntityType::getScopeString)
            .or(
                () ->
                    this.argumentDeserializer.deserializePrimitive(
                        arguments, EntityScopeArgument.class))
            .orElseThrow();

    return this.build(context, arguments, entityScope, selectionSet);
  }

  private Single<EntityRequest> build(
      GraphQlRequestContext context,
      Map<String, Object> arguments,
      String scope,
      DataFetchingFieldSelectionSet selectionSet) {
    boolean includeInactive =
        this.argumentDeserializer
            .deserializePrimitive(arguments, IncludeInactiveArgument.class)
            .orElse(false);

    boolean fetchTotal =
        this.selectionFinder
                .findSelections(
                    selectionSet, SelectionQuery.namedChild(ResultSet.RESULT_SET_TOTAL_NAME))
                .count()
            > 0;

    return zip(
        this.resultSetRequestBuilder.build(
            context, scope, arguments, selectionSet, AggregatableOrderArgument.class),
        this.metricRequestBuilder.build(context, scope, this.getResultSets(selectionSet)),
        this.edgeRequestBuilder.buildIncomingEdgeRequest(
            context,
            this.timeRange(arguments),
            this.space(arguments),
            this.getIncomingEdges(selectionSet)),
        this.edgeRequestBuilder.buildOutgoingEdgeRequest(
            context,
            this.timeRange(arguments),
            this.space(arguments),
            this.getOutgoingEdges(selectionSet)),
        (resultSetRequest, metricRequestList, incomingEdges, outgoingEdges) ->
            new DefaultEntityRequest(
                scope,
                resultSetRequest,
                metricRequestList,
                incomingEdges,
                outgoingEdges,
                includeInactive,
                fetchTotal));
  }

  private Stream<SelectedField> getResultSets(DataFetchingFieldSelectionSet selectionSet) {
    return this.selectionFinder.findSelections(
        selectionSet, SelectionQuery.namedChild(ResultSet.RESULT_SET_RESULTS_NAME));
  }

  private Stream<SelectedField> getIncomingEdges(DataFetchingFieldSelectionSet selectionSet) {
    return this.selectionFinder.findSelections(
        selectionSet,
        SelectionQuery.builder()
            .selectionPath(List.of(ResultSet.RESULT_SET_RESULTS_NAME, ENTITY_INCOMING_EDGES_KEY))
            .build());
  }

  private Stream<SelectedField> getOutgoingEdges(DataFetchingFieldSelectionSet selectionSet) {
    return this.selectionFinder.findSelections(
        selectionSet,
        SelectionQuery.builder()
            .selectionPath(List.of(ResultSet.RESULT_SET_RESULTS_NAME, ENTITY_OUTGOING_EDGES_KEY))
            .build());
  }

  private TimeRangeArgument timeRange(Map<String, Object> arguments) {
    return this.argumentDeserializer
        .deserializeObject(arguments, TimeRangeArgument.class)
        .orElseThrow();
  }

  private Optional<String> space(Map<String, Object> arguments) {
    return this.argumentDeserializer.deserializePrimitive(arguments, SpaceArgument.class);
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultEntityRequest implements EntityRequest {
    String entityType;
    ResultSetRequest<AggregatableOrderArgument> resultSetRequest;
    List<MetricRequest> metricRequests;
    EdgeSetGroupRequest incomingEdgeRequests;
    EdgeSetGroupRequest outgoingEdgeRequests;
    boolean includeInactive;
    boolean fetchTotal;
  }
}
