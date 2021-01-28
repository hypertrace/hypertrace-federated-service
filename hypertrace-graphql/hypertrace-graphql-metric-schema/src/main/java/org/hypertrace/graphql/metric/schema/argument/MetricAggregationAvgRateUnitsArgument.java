package org.hypertrace.graphql.metric.schema.argument;

import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface MetricAggregationAvgRateUnitsArgument extends PrimitiveArgument<TimeUnit> {
  String ARGUMENT_NAME = "units";
}
