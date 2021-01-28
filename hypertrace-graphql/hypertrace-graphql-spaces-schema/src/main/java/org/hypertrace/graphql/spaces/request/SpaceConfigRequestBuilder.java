package org.hypertrace.graphql.spaces.request;

import java.util.Map;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

public interface SpaceConfigRequestBuilder {
  SpaceConfigRuleCreationRequest buildCreationRequest(
      GraphQlRequestContext requestContext, Map<String, Object> arguments);

  SpaceConfigRuleUpdateRequest buildUpdateRequest(
      GraphQlRequestContext requestContext, Map<String, Object> arguments);

  SpaceConfigRuleDeleteRequest buildDeleteRequest(
      GraphQlRequestContext requestContext, Map<String, Object> arguments);
}
