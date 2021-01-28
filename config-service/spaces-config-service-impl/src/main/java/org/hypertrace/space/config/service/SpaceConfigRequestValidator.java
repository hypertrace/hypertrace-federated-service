package org.hypertrace.space.config.service;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;

public interface SpaceConfigRequestValidator {

  Single<CreateRuleRequest> validateCreateRequest(RequestContext requestContext, CreateRuleRequest createRuleRequest);

  Single<GetRulesRequest> validateGetRequest(RequestContext requestContext, GetRulesRequest getRulesRequest);

  Single<UpdateRuleRequest> validateUpdateRequest(RequestContext requestContext, UpdateRuleRequest updateRuleRequest);

  Single<DeleteRuleRequest> validateDeleteRequest(RequestContext requestContext, DeleteRuleRequest deleteRuleRequest);
}
