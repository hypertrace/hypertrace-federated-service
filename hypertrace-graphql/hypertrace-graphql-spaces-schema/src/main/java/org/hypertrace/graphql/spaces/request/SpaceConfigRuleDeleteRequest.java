package org.hypertrace.graphql.spaces.request;

import org.hypertrace.core.graphql.context.GraphQlRequestContext;

public interface SpaceConfigRuleDeleteRequest {
  GraphQlRequestContext context();

  String id();
}
