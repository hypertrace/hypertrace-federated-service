package org.hypertrace.graphql.spaces.schema.shared;

import graphql.annotations.annotationTypes.GraphQLName;
import org.hypertrace.core.graphql.common.schema.id.Identifiable;

@GraphQLName(SpaceConfigRule.TYPE_NAME)
public interface SpaceConfigRule extends Identifiable, SpaceConfigRuleDefinition {
  String ARGUMENT_NAME = "rule";
  String TYPE_NAME = "SpaceConfigRule";
}
