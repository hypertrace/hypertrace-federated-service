package org.hypertrace.graphql.entity.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeQueryable;
import org.hypertrace.core.graphql.common.schema.id.Identifiable;
import org.hypertrace.core.graphql.common.schema.type.Typed;
import org.hypertrace.graphql.entity.schema.argument.NeighborEntityScopeArgument;
import org.hypertrace.graphql.entity.schema.argument.NeighborEntityTypeArgument;
import org.hypertrace.graphql.metric.schema.MetricQueryable;

@GraphQLName(Entity.TYPE_NAME)
public interface Entity extends AttributeQueryable, MetricQueryable, Identifiable, Typed<String> {
  String TYPE_NAME = "Entity";
  String ENTITY_INCOMING_EDGES_KEY = "incomingEdges";
  String ENTITY_OUTGOING_EDGES_KEY = "outgoingEdges";

  @Override
  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(TYPE_FIELD_NAME)
  String type();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(ENTITY_INCOMING_EDGES_KEY)
  EdgeResultSet incomingEdges(
      @GraphQLName(NeighborEntityTypeArgument.ARGUMENT_NAME) EntityType neighborType,
      @GraphQLName(NeighborEntityScopeArgument.ARGUMENT_NAME) String neighborScope);

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(ENTITY_OUTGOING_EDGES_KEY)
  EdgeResultSet outgoingEdges(
      @GraphQLName(NeighborEntityTypeArgument.ARGUMENT_NAME) EntityType neighborType,
      @GraphQLName(NeighborEntityScopeArgument.ARGUMENT_NAME) String neighborScope);
}
