package org.hypertrace.graphql.entity.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;
import org.hypertrace.graphql.entity.schema.EntityType;

public interface NeighborEntityScopeArgument extends PrimitiveArgument<String> {
  String ARGUMENT_NAME = "neighborScope";
}
