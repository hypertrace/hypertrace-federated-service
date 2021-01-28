package org.hypertrace.graphql.explorer.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface SelectionSizeArgument extends PrimitiveArgument<Integer> {
  String ARGUMENT_NAME = "size";
}
