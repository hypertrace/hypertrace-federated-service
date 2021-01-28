package org.hypertrace.graphql.metric.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface MetricKeyArgument extends PrimitiveArgument<String> {
  String ARGUMENT_NAME = "key";
}
