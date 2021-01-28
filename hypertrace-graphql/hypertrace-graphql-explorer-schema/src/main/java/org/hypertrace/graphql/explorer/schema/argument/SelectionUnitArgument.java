package org.hypertrace.graphql.explorer.schema.argument;

import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface SelectionUnitArgument extends PrimitiveArgument<TimeUnit> {
  String ARGUMENT_NAME = "units";
}
