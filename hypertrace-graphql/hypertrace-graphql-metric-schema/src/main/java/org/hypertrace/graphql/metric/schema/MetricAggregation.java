package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

@GraphQLName(MetricAggregation.METRIC_AGGREGATION_TYPE_NAME)
public interface MetricAggregation {
  String METRIC_AGGREGATION_TYPE_NAME = "MetricAggregation";
  String METRIC_AGGREGATION_VALUE_KEY = "value";

  // TODO do we want to support integer typed metrics for things like count?
  @GraphQLName(METRIC_AGGREGATION_VALUE_KEY)
  @GraphQLField
  Double value();

}
