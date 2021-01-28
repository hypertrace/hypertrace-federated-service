package org.hypertrace.graphql.spaces.schema.query;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.results.ResultSet;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;

@GraphQLName(SpaceConfigRuleResultSet.TYPE_NAME)
public interface SpaceConfigRuleResultSet extends ResultSet<SpaceConfigRule> {
  String TYPE_NAME = "SpaceConfigRuleResultSet";

  @Override
  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(RESULT_SET_RESULTS_NAME)
  List<SpaceConfigRule> results();
}
