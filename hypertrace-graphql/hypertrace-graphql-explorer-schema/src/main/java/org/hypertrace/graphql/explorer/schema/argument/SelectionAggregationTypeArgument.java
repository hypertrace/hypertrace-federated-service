package org.hypertrace.graphql.explorer.schema.argument;

import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface SelectionAggregationTypeArgument extends PrimitiveArgument<MetricAggregationType> {
  String ARGUMENT_NAME = "aggregation";
}
