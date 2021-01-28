package org.hypertrace.graphql.spaces.schema.shared;

import graphql.annotations.annotationTypes.GraphQLName;

@GraphQLName(SpaceConfigRuleType.TYPE_NAME)
public enum SpaceConfigRuleType {
  ATTRIBUTE_VALUE;

  public static final String TYPE_NAME = "SpaceConfigRuleType";
}
