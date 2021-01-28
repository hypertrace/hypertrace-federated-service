package org.hypertrace.graphql.spaces.request;

import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleDefinition;

public interface SpaceConfigRuleCreationRequest {
  GraphQlRequestContext context();

  SpaceConfigRuleDefinition ruleDefinition();
}
