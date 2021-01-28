package org.hypertrace.graphql.spaces.dao;

import org.hypertrace.graphql.spaces.request.SpaceConfigRuleCreationRequest;
import org.hypertrace.graphql.spaces.request.SpaceConfigRuleDeleteRequest;
import org.hypertrace.graphql.spaces.request.SpaceConfigRuleUpdateRequest;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleAttributeValueRule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleDefinition;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;

class SpaceConfigRulesRequestConverter {

  CreateRuleRequest convertCreationRequest(SpaceConfigRuleCreationRequest creationRequest) {
    return this.buildCreationRequestForRule(creationRequest.ruleDefinition());
  }

  GetRulesRequest convertGetRequest() {
    return GetRulesRequest.getDefaultInstance();
  }

  DeleteRuleRequest convertDeleteRequest(SpaceConfigRuleDeleteRequest deleteRequest) {
    return DeleteRuleRequest.newBuilder().setId(deleteRequest.id()).build();
  }

  UpdateRuleRequest convertUpdateRequest(SpaceConfigRuleUpdateRequest updateRequest) {
    return UpdateRuleRequest.newBuilder()
        .setUpdatedRule(this.convertRule(updateRequest.rule()))
        .build();
  }

  private CreateRuleRequest buildCreationRequestForRule(SpaceConfigRuleDefinition ruleDefinition) {
    switch (ruleDefinition.type()) {
      case ATTRIBUTE_VALUE:
        return CreateRuleRequest.newBuilder()
            .setAttributeValueRuleData(
                this.buildAttributeValueRuleData(ruleDefinition.attributeValueRule()))
            .build();
      default:
        throw new UnsupportedOperationException(
            "Cannot create unknown rule definition type: " + ruleDefinition.type().name());
    }
  }

  private SpaceConfigRule convertRule(
      org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule rule) {
    switch (rule.type()) {
      case ATTRIBUTE_VALUE:
        return SpaceConfigRule.newBuilder()
            .setId(rule.id())
            .setAttributeValueRuleData(this.buildAttributeValueRuleData(rule.attributeValueRule()))
            .build();
      default:
        throw new UnsupportedOperationException(
            "Cannot convert unknown rule type: " + rule.type().name());
    }
  }

  private AttributeValueRuleData buildAttributeValueRuleData(
      SpaceConfigRuleAttributeValueRule attributeValueRule) {
    return AttributeValueRuleData.newBuilder()
        .setAttributeKey(attributeValueRule.attributeKey())
        .setAttributeScope(attributeValueRule.attributeScope())
        .build();
  }
}
