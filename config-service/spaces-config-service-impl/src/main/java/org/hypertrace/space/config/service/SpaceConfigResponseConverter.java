package org.hypertrace.space.config.service;

import org.hypertrace.config.service.v1.DeleteConfigResponse;
import org.hypertrace.config.service.v1.GetAllConfigsResponse;
import org.hypertrace.config.service.v1.UpsertConfigResponse;
import org.hypertrace.spaces.config.service.v1.CreateRuleResponse;
import org.hypertrace.spaces.config.service.v1.DeleteRuleResponse;
import org.hypertrace.spaces.config.service.v1.GetRulesResponse;
import org.hypertrace.spaces.config.service.v1.UpdateRuleResponse;

public interface SpaceConfigResponseConverter {
  CreateRuleResponse convertCreateResponse(UpsertConfigResponse upsertConfigResponse);

  UpdateRuleResponse convertUpdateResponse(UpsertConfigResponse upsertConfigResponse);

  GetRulesResponse convertGetResponse(GetAllConfigsResponse getAllConfigsResponse);

  DeleteRuleResponse convertDeleteResponse(DeleteConfigResponse deleteConfigResponse);
}
