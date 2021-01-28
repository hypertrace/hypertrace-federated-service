package org.hypertrace.space.config.service;

import org.hypertrace.config.service.v1.DeleteConfigRequest;
import org.hypertrace.config.service.v1.GetAllConfigsRequest;
import org.hypertrace.config.service.v1.UpsertConfigRequest;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;

public interface SpaceConfigRequestConverter {

  UpsertConfigRequest convertCreateRequest(CreateRuleRequest createRuleRequest);

  UpsertConfigRequest convertUpdateRequest(UpdateRuleRequest updateRuleRequest);

  GetAllConfigsRequest convertGetRequest(GetRulesRequest getRulesRequest);

  DeleteConfigRequest convertDeleteRequest(DeleteRuleRequest deleteRuleRequest);
}
