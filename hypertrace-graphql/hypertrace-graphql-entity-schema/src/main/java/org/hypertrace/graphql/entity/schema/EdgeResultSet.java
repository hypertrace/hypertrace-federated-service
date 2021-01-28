package org.hypertrace.graphql.entity.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.results.ResultSet;

@GraphQLName(EdgeResultSet.TYPE_NAME)
public interface EdgeResultSet extends ResultSet<Edge> {
  String TYPE_NAME = "EdgeResultSet";

  @Override
  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(RESULT_SET_RESULTS_NAME)
  List<Edge> results();
}
