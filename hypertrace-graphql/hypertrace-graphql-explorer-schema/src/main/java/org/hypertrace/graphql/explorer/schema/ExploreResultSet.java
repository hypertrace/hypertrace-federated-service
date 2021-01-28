package org.hypertrace.graphql.explorer.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.results.ResultSet;

@GraphQLName(ExploreResultSet.TYPE_NAME)
public interface ExploreResultSet extends ResultSet<ExploreResult> {
  String TYPE_NAME = "ExploreResultSet";

  @Override
  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(RESULT_SET_RESULTS_NAME)
  List<ExploreResult> results();
}
