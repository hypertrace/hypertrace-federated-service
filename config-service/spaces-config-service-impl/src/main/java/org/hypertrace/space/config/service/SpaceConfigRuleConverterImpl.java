package org.hypertrace.space.config.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Value;
import org.hypertrace.config.proto.converter.ConfigProtoConverter;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;

class SpaceConfigRuleConverterImpl implements SpaceConfigRuleConverter {

  @Override
  public Value convertToGeneric(SpaceConfigRule rule) {
    try {
      return ConfigProtoConverter.convertToValue(rule);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public SpaceConfigRule convertFromGeneric(Value config) {
    SpaceConfigRule.Builder builder = SpaceConfigRule.newBuilder();
    try {
      ConfigProtoConverter.mergeFromValue(config, builder);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
    return builder.build();
  }
}
