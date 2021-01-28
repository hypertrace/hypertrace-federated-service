package org.hypertrace.space.config.service;

import java.util.List;
import java.util.stream.Collectors;
import org.hypertrace.config.service.v1.DeleteConfigResponse;
import org.hypertrace.config.service.v1.GetAllConfigsResponse;
import org.hypertrace.config.service.v1.UpsertConfigResponse;
import org.hypertrace.spaces.config.service.v1.CreateRuleResponse;
import org.hypertrace.spaces.config.service.v1.DeleteRuleResponse;
import org.hypertrace.spaces.config.service.v1.GetRulesResponse;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.UpdateRuleResponse;

class SpaceConfigResponseConverterImpl implements SpaceConfigResponseConverter {
  private final SpaceConfigRuleConverter ruleConverter = new SpaceConfigRuleConverterImpl();

  @Override
  public CreateRuleResponse convertCreateResponse(UpsertConfigResponse upsertConfigResponse) {
    return CreateRuleResponse.newBuilder()
        .setRule(this.ruleConverter.convertFromGeneric(upsertConfigResponse.getConfig()))
        .build();
  }

  @Override
  public UpdateRuleResponse convertUpdateResponse(UpsertConfigResponse upsertConfigResponse) {
    return UpdateRuleResponse.newBuilder()
        .setRule(this.ruleConverter.convertFromGeneric(upsertConfigResponse.getConfig()))
        .build();
  }

  @Override
  public GetRulesResponse convertGetResponse(GetAllConfigsResponse getAllConfigsResponse) {
    List<SpaceConfigRule> rules =
        getAllConfigsResponse.getContextSpecificConfigsList().stream()
            .map(wrapper -> this.ruleConverter.convertFromGeneric(wrapper.getConfig()))
            .collect(Collectors.toList());

    return GetRulesResponse.newBuilder().addAllRules(rules).build();
  }

  @Override
  public DeleteRuleResponse convertDeleteResponse(DeleteConfigResponse deleteConfigResponse) {
    return DeleteRuleResponse.getDefaultInstance();
  }
}
