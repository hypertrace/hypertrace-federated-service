package org.hypertrace.graphql.spaces.schema.query;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.results.ResultSet;

@GraphQLName(SpaceResultSet.TYPE_NAME)
public interface SpaceResultSet extends ResultSet<Space> {
  String TYPE_NAME = "SpaceResultSet";

  @Override
  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(RESULT_SET_RESULTS_NAME)
  List<Space> results();
}
