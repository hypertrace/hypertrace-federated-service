package org.hypertrace.graphql.spaces.schema.query;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.graphql.spaces.fetcher.SpaceConfigRuleFetcher;
import org.hypertrace.graphql.spaces.fetcher.SpacesFetcher;

public interface SpacesQuerySchema {

  String SPACE_CONFIG_RULES_KEY = "spaceConfigRules";
  String SPACES_KEY = "spaces";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(SPACE_CONFIG_RULES_KEY)
  @GraphQLDataFetcher(SpaceConfigRuleFetcher.class)
  SpaceConfigRuleResultSet spaceConfigRules();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(SPACES_KEY)
  @GraphQLDataFetcher(SpacesFetcher.class)
  SpaceResultSet spaces();
}
