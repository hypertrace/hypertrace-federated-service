package org.hypertrace.graphql.entity.schema;

import static org.hypertrace.graphql.entity.schema.Edge.EDGE_TYPE_NAME;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeQueryable;
import org.hypertrace.graphql.metric.schema.MetricAggregationQueryable;

@GraphQLName(EDGE_TYPE_NAME)
public interface Edge extends AttributeQueryable, MetricAggregationQueryable {
  String EDGE_TYPE_NAME = "Edge";
  String EDGE_NEIGHBOR_KEY = "neighbor";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(EDGE_NEIGHBOR_KEY)
  Entity neighbor();
}
