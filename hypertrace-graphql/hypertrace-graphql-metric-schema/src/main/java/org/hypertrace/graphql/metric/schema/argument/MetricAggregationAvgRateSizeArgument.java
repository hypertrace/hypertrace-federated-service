package org.hypertrace.graphql.metric.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface MetricAggregationAvgRateSizeArgument extends PrimitiveArgument<Integer> {
  String ARGUMENT_NAME = "size";
}
