package org.hypertrace.graphql.explorer.schema.argument;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;

@GraphQLName(GroupByArgument.TYPE_NAME)
public interface GroupByArgument {
  String TYPE_NAME = "GroupByArgument";
  String ARGUMENT_NAME = "groupBy";

  String GROUP_BY_KEYS_KEY = "keys";
  String GROUP_BY_INCLUDE_REST_KEY = "includeRest";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(GROUP_BY_KEYS_KEY)
  List<String> keys();

  @GraphQLField
  @GraphQLName(GROUP_BY_INCLUDE_REST_KEY)
  boolean includeRest();
}
