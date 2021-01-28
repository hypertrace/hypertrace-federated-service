package org.hypertrace.space.config.service;

import org.hypertrace.config.service.v1.DeleteConfigRequest;
import org.hypertrace.config.service.v1.GetAllConfigsRequest;
import org.hypertrace.config.service.v1.UpsertConfigRequest;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;

class SpaceConfigRequestConverterImpl implements SpaceConfigRequestConverter {
  static final String RESOURCE_NAME = "SpacesConfigRule";
  static final String RESOURCE_NAMESPACE = "SpacesConfigService";

  private final SpaceConfigRuleConverter ruleConverter = new SpaceConfigRuleConverterImpl();
  private final SpaceConfigRuleIdGenerator idGenerator;

  SpaceConfigRequestConverterImpl(SpaceConfigRuleIdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  @Override
  public UpsertConfigRequest convertCreateRequest(CreateRuleRequest createRuleRequest) {
    return this.buildUpsertForRule(this.generateRule(createRuleRequest));
  }

  @Override
  public UpsertConfigRequest convertUpdateRequest(UpdateRuleRequest updateRuleRequest) {
    return this.buildUpsertForRule(updateRuleRequest.getUpdatedRule());
  }

  @Override
  public GetAllConfigsRequest convertGetRequest(GetRulesRequest getRulesRequest) {
    return GetAllConfigsRequest.newBuilder()
        .setResourceName(RESOURCE_NAME)
        .setResourceNamespace(RESOURCE_NAMESPACE)
        .build();
  }

  @Override
  public DeleteConfigRequest convertDeleteRequest(DeleteRuleRequest deleteRuleRequest) {
    return DeleteConfigRequest.newBuilder()
        .setResourceName(RESOURCE_NAME)
        .setResourceNamespace(RESOURCE_NAMESPACE)
        .setContext(deleteRuleRequest.getId())
        .build();
  }

  private UpsertConfigRequest buildUpsertForRule(SpaceConfigRule rule) {
    return UpsertConfigRequest.newBuilder()
        .setContext(rule.getId())
        .setConfig(this.ruleConverter.convertToGeneric(rule))
        .setResourceName(RESOURCE_NAME)
        .setResourceNamespace(RESOURCE_NAMESPACE)
        .build();
  }

  private SpaceConfigRule generateRule(CreateRuleRequest request) {
    SpaceConfigRule.Builder ruleBuilder =
        SpaceConfigRule.newBuilder().setId(this.idGenerator.generateId());

    switch (request.getRuleDataCase()) {
      case ATTRIBUTE_VALUE_RULE_DATA:
        return ruleBuilder.setAttributeValueRuleData(request.getAttributeValueRuleData()).build();
      case RULEDATA_NOT_SET:
      default:
        return ruleBuilder.build();
    }
  }
}
