package org.hypertrace.graphql.entity.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface IncludeInactiveArgument extends PrimitiveArgument<Boolean> {
  String ARGUMENT_NAME = "includeInactive";
}
