package org.hypertrace.graphql.entity.dao;

import lombok.Value;
import org.hypertrace.gateway.service.v1.entity.Entity;
import org.hypertrace.gateway.service.v1.entity.EntityInteraction;

@Value
class InteractionResponse {
  // Create a wrapper around interaction to better capture its identity; source + interaction
  // Should always be unique
  Entity source;
  EntityInteraction interaction;
}
