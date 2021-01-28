package org.hypertrace.space.config.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;

class SpaceConfigRequestValidatorImpl implements SpaceConfigRequestValidator {
  @Override
  public Single<CreateRuleRequest> validateCreateRequest(
      RequestContext requestContext, CreateRuleRequest createRuleRequest) {
    return this.validateTenant(requestContext)
        .andThen(
            this.check(
                createRuleRequest.hasAttributeValueRuleData(),
                "Only Attribute Value rules supported"))
        .andThen(this.validateAttributeValueRule(createRuleRequest.getAttributeValueRuleData()))
        .toSingleDefault(createRuleRequest);
  }

  @Override
  public Single<GetRulesRequest> validateGetRequest(
      RequestContext requestContext, GetRulesRequest getRulesRequest) {
    return this.validateTenant(requestContext).toSingleDefault(getRulesRequest);
  }

  @Override
  public Single<UpdateRuleRequest> validateUpdateRequest(
      RequestContext requestContext, UpdateRuleRequest updateRuleRequest) {
    return this.validateTenant(requestContext)
        .andThen(this.validateRule(updateRuleRequest.getUpdatedRule()))
        .toSingleDefault(updateRuleRequest);
  }

  @Override
  public Single<DeleteRuleRequest> validateDeleteRequest(
      RequestContext requestContext, DeleteRuleRequest deleteRuleRequest) {
    return this.validateTenant(requestContext)
        .andThen(this.check(!deleteRuleRequest.getId().isEmpty(), "ID missing from delete request"))
        .toSingleDefault(deleteRuleRequest);
  }

  private Completable validateRule(SpaceConfigRule rule) {
    return this.check(!rule.getId().isEmpty(), "Rule ID missing from update request")
        .andThen(
            this.check(rule.hasAttributeValueRuleData(), "Only Attribute Value rules supported"))
        .andThen(this.validateAttributeValueRule(rule.getAttributeValueRuleData()));
  }

  private Completable validateAttributeValueRule(AttributeValueRuleData attributeValueRuleData) {
    return this.check(
            !attributeValueRuleData.getAttributeScope().isEmpty(),
            "Attribute value rule missing attribute scope")
        .andThen(
            this.check(
                !attributeValueRuleData.getAttributeKey().isEmpty(),
                "Attribute value rule missing attribute key"));
  }

  private Completable validateTenant(RequestContext requestContext) {
    return this.check(
        requestContext.getTenantId().isPresent(), "Tenant ID is not present in request context");
  }

  private Completable check(boolean value, String errorMessage) {
    return value
        ? Completable.complete()
        : Completable.error(new IllegalArgumentException(errorMessage));
  }
}
