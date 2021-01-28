package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.graphql.metric.schema.argument.MetricKeyArgument;

public interface MetricQueryable extends MetricAggregationQueryable {
  @GraphQLField
  @GraphQLName(METRIC_FIELD_NAME)
  @GraphQLNonNull
  @Override
  MetricContainer metric(@GraphQLName(MetricKeyArgument.ARGUMENT_NAME) @GraphQLNonNull String key);
}
