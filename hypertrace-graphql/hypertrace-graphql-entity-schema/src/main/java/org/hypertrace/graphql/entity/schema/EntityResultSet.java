package org.hypertrace.graphql.entity.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.results.ResultSet;

@GraphQLName(EntityResultSet.TYPE_NAME)
public interface EntityResultSet extends ResultSet<Entity> {
  String TYPE_NAME = "EntityResultSet";

  @Override
  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(RESULT_SET_RESULTS_NAME)
  List<Entity> results();
}
