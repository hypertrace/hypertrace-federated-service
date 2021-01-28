package org.hypertrace.space.config.service;

import java.util.UUID;

class SpaceConfigRuleIdGenerator {

  String generateId() {
    return UUID.randomUUID().toString();
  }
}
