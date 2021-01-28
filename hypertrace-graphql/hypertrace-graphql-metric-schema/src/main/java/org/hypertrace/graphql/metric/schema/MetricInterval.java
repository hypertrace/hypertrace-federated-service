package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.time.Instant;

public interface MetricInterval extends MetricAggregationContainer {
  String METRIC_INTERVAL_TYPE_NAME = "MetricInterval";

  @GraphQLField
  @GraphQLNonNull
  Instant startTime();

  @GraphQLField
  @GraphQLNonNull
  Instant endTime();
}
