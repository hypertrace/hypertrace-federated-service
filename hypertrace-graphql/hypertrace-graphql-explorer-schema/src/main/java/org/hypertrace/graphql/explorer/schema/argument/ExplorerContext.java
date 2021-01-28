package org.hypertrace.graphql.explorer.schema.argument;

import graphql.annotations.annotationTypes.GraphQLName;

@GraphQLName(ExplorerContext.TYPE_NAME)
public interface ExplorerContext {
  String TYPE_NAME = "ExplorerContext";

  String getScopeString();
}
