package org.hypertrace.graphql.spaces.schema.shared;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;

@GraphQLName(SpaceConfigRuleDefinition.TYPE_NAME)
public interface SpaceConfigRuleDefinition {
  String TYPE_NAME = "SpaceConfigRuleDefinition";
  String ARGUMENT_NAME = "definition";
  String SPACE_CONFIG_RULE_DEFINITION_TYPE_KEY = "type";
  String SPACE_CONFIG_RULE_DEFINITION_ATTRIBUTE_VALUE_RULE_KEY = "attributeValueRule";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(SPACE_CONFIG_RULE_DEFINITION_TYPE_KEY)
  SpaceConfigRuleType type();

  @GraphQLField
  @GraphQLName(SPACE_CONFIG_RULE_DEFINITION_ATTRIBUTE_VALUE_RULE_KEY)
  SpaceConfigRuleAttributeValueRule attributeValueRule();
}
