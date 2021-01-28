package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.graphql.metric.schema.argument.MetricKeyArgument;

public interface MetricAggregationQueryable {
  String METRIC_FIELD_NAME = "metric";

  @GraphQLField
  @GraphQLName(METRIC_FIELD_NAME)
  @GraphQLNonNull
  BaselinedMetricAggregationContainer metric(
      @GraphQLName(MetricKeyArgument.ARGUMENT_NAME) @GraphQLNonNull String key);
}
