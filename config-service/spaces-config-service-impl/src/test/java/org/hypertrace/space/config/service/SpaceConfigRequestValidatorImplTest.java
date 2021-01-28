package org.hypertrace.space.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpaceConfigRequestValidatorImplTest {

  @Mock RequestContext requestContext;

  SpaceConfigRequestValidator requestValidator = new SpaceConfigRequestValidatorImpl();

  @Test
  void validatesCreateRequest() {
    CreateRuleRequest validRequest =
        CreateRuleRequest.newBuilder()
            .setAttributeValueRuleData(
                AttributeValueRuleData.newBuilder()
                    .setAttributeScope("scope")
                    .setAttributeKey("key"))
            .build();

    CreateRuleRequest invalidRequest =
        validRequest.toBuilder()
            .setAttributeValueRuleData(
                AttributeValueRuleData.newBuilder().setAttributeScope("scope")) // missing key
            .build();

    when(requestContext.getTenantId()).thenReturn(Optional.empty());
    // Valid request, no tenant
    assertThrows(
        IllegalArgumentException.class,
        () -> requestValidator.validateCreateRequest(requestContext, validRequest).blockingGet());

    when(requestContext.getTenantId()).thenReturn(Optional.of("tenant-id"));

    // Missing data entirely on request, valid tenant
    assertThrows(
        IllegalArgumentException.class,
        () ->
            requestValidator
                .validateCreateRequest(requestContext, CreateRuleRequest.getDefaultInstance())
                .blockingGet());

    // Invalid request, valid tenant
    assertThrows(
        IllegalArgumentException.class,
        () -> requestValidator.validateCreateRequest(requestContext, invalidRequest).blockingGet());

    // Valid request, valid tenant
    assertEquals(
        validRequest,
        requestValidator.validateCreateRequest(requestContext, validRequest).blockingGet());
  }

  @Test
  void validatesGetRequest() {
    // no data in get request, nothing to validate
    GetRulesRequest request = GetRulesRequest.getDefaultInstance();

    // No tenant
    when(requestContext.getTenantId()).thenReturn(Optional.empty());
    assertThrows(
        IllegalArgumentException.class,
        () -> requestValidator.validateGetRequest(requestContext, request).blockingGet());

    // Valid tenant
    when(requestContext.getTenantId()).thenReturn(Optional.of("tenant-id"));
    assertEquals(
        request, requestValidator.validateGetRequest(requestContext, request).blockingGet());
  }

  @Test
  void validatesDeleteRequest() {
    DeleteRuleRequest validRequest = DeleteRuleRequest.newBuilder().setId("some-id").build();
    DeleteRuleRequest invalidRequest = DeleteRuleRequest.getDefaultInstance();

    when(requestContext.getTenantId()).thenReturn(Optional.empty());
    // Valid request, no tenant
    assertThrows(
        IllegalArgumentException.class,
        () -> requestValidator.validateDeleteRequest(requestContext, validRequest).blockingGet());

    when(requestContext.getTenantId()).thenReturn(Optional.of("tenant-id"));

    // Invalid request, valid tenant
    assertThrows(
        IllegalArgumentException.class,
        () -> requestValidator.validateDeleteRequest(requestContext, invalidRequest).blockingGet());

    // Valid request, valid tenant
    assertEquals(
        validRequest,
        requestValidator.validateDeleteRequest(requestContext, validRequest).blockingGet());
  }

  @Test
  void validateUpdateRequest() {
    UpdateRuleRequest validRequest =
        UpdateRuleRequest.newBuilder()
            .setUpdatedRule(
                SpaceConfigRule.newBuilder()
                    .setId("id")
                    .setAttributeValueRuleData(
                        AttributeValueRuleData.newBuilder()
                            .setAttributeScope("scope")
                            .setAttributeKey("key")))
            .build();

    UpdateRuleRequest invalidRequestNoId =
        validRequest.toBuilder()
            .setUpdatedRule(validRequest.getUpdatedRule().toBuilder().clearId())
            .build();

    UpdateRuleRequest invalidRequestNoKey =
        validRequest.toBuilder()
            .setUpdatedRule(
                validRequest.getUpdatedRule().toBuilder()
                    .setAttributeValueRuleData(
                        validRequest.getUpdatedRule().getAttributeValueRuleData().toBuilder()
                            .clearAttributeKey()))
            .build();

    when(requestContext.getTenantId()).thenReturn(Optional.empty());
    // Valid request, no tenant
    assertThrows(
        IllegalArgumentException.class,
        () -> requestValidator.validateUpdateRequest(requestContext, validRequest).blockingGet());

    when(requestContext.getTenantId()).thenReturn(Optional.of("tenant-id"));

    // Invalid request missing id
    assertThrows(
        IllegalArgumentException.class,
        () ->
            requestValidator
                .validateUpdateRequest(requestContext, invalidRequestNoId)
                .blockingGet());

    // Invalid request missing attribute key
    assertThrows(
        IllegalArgumentException.class,
        () ->
            requestValidator
                .validateUpdateRequest(requestContext, invalidRequestNoKey)
                .blockingGet());

    // Valid request, valid tenant
    assertEquals(
        validRequest,
        requestValidator.validateUpdateRequest(requestContext, validRequest).blockingGet());
  }
}
