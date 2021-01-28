package org.hypertrace.graphql.entity.schema.argument;

import org.hypertrace.core.graphql.deserialization.PrimitiveArgument;
import org.hypertrace.graphql.entity.schema.EntityType;

public interface EntityTypeArgument extends PrimitiveArgument<EntityType> {
  String ARGUMENT_NAME = "type";
}
