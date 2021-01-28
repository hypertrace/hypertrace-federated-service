package org.hypertrace.graphql.explorer.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeType;
import org.hypertrace.core.graphql.common.schema.type.Typed;

public interface Selection extends Typed<AttributeType> {
  String SELECTION_VALUE_KEY = "value";

  @GraphQLField
  @GraphQLName(SELECTION_VALUE_KEY)
  Object value();

  @Override
  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(TYPE_FIELD_NAME)
  AttributeType type();
}
