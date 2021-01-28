package org.hypertrace.graphql.spaces.request;

import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;

public interface SpaceConfigRuleUpdateRequest {
  GraphQlRequestContext context();

  SpaceConfigRule rule();
}
