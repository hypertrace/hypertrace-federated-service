package org.hypertrace.graphql.explorer.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface ExplorerContextArgument extends PrimitiveArgument<ExplorerContext> {
  String ARGUMENT_NAME = "context";
}
