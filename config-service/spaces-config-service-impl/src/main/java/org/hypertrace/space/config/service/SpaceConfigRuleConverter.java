package org.hypertrace.space.config.service;

import com.google.protobuf.Value;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;

public interface SpaceConfigRuleConverter {

  Value convertToGeneric(SpaceConfigRule rule);

  SpaceConfigRule convertFromGeneric(Value config);
}
