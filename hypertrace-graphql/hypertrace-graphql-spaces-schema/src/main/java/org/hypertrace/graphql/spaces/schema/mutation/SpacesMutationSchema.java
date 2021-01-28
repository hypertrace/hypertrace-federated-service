package org.hypertrace.graphql.spaces.schema.mutation;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.graphql.spaces.deserialization.SpaceRuleIdArgument;
import org.hypertrace.graphql.spaces.mutator.SpaceConfigRuleCreationMutator;
import org.hypertrace.graphql.spaces.mutator.SpaceConfigRuleDeleteMutator;
import org.hypertrace.graphql.spaces.mutator.SpaceConfigRuleUpdateMutator;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleDefinition;

public interface SpacesMutationSchema {

  String CREATE_SPACE_CONFIG_RULE_KEY = "createSpaceConfigRule";
  String UPDATE_SPACE_CONFIG_RULE_KEY = "updateSpaceConfigRule";
  String DELETE_SPACE_CONFIG_RULE_KEY = "deleteSpaceConfigRule";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(CREATE_SPACE_CONFIG_RULE_KEY)
  @GraphQLDataFetcher(SpaceConfigRuleCreationMutator.class)
  SpaceConfigRule createSpaceConfigRule(
      @GraphQLNonNull @GraphQLName(SpaceConfigRuleDefinition.ARGUMENT_NAME)
          SpaceConfigRuleDefinition definition);

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(UPDATE_SPACE_CONFIG_RULE_KEY)
  @GraphQLDataFetcher(SpaceConfigRuleUpdateMutator.class)
  SpaceConfigRule updateSpaceConfigRule(
      @GraphQLNonNull @GraphQLName(SpaceConfigRule.ARGUMENT_NAME) SpaceConfigRule rule);

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(DELETE_SPACE_CONFIG_RULE_KEY)
  @GraphQLDataFetcher(SpaceConfigRuleDeleteMutator.class)
  Boolean deleteSpaceConfigRule(
      @GraphQLNonNull @GraphQLName(SpaceRuleIdArgument.ARGUMENT_NAME) String id);
}
