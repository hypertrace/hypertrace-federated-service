package org.hypertrace.graphql.explorer.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface SelectionKeyArgument extends PrimitiveArgument<String> {
  String ARGUMENT_NAME = "key";
}
