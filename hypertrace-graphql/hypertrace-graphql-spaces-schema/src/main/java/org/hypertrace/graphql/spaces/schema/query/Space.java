package org.hypertrace.graphql.spaces.schema.query;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;

@GraphQLName(Space.TYPE_NAME)
public interface Space {
  String TYPE_NAME = "Space";
  String SPACE_NAME_KEY = "name";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(SPACE_NAME_KEY)
  String name();
}
