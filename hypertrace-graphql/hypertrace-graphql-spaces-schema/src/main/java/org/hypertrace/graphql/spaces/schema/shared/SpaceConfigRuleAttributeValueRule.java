package org.hypertrace.graphql.spaces.schema.shared;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;

@GraphQLName(SpaceConfigRuleAttributeValueRule.TYPE_NAME)
public interface SpaceConfigRuleAttributeValueRule {

  String TYPE_NAME = "SpaceConfigRuleAttributeValueRule";
  String ATTRIBUTE_VALUE_ATTRIBUTE_SCOPE_KEY = "attributeScope";
  String ATTRIBUTE_VALUE_ATTRIBUTE_KEY_KEY = "attributeKey";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(ATTRIBUTE_VALUE_ATTRIBUTE_SCOPE_KEY)
  String attributeScope();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(ATTRIBUTE_VALUE_ATTRIBUTE_KEY_KEY)
  String attributeKey();
}
