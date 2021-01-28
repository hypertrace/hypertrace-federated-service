package org.hypertrace.graphql.spaces.deserialization;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;

public interface SpaceRuleIdArgument extends PrimitiveArgument<String> {
  String ARGUMENT_NAME = "id";
}
