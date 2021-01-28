package org.hypertrace.space.config.service;

import static org.hypertrace.space.config.service.SpaceConfigRequestConverterImpl.RESOURCE_NAME;
import static org.hypertrace.space.config.service.SpaceConfigRequestConverterImpl.RESOURCE_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.Map;
import org.hypertrace.config.service.v1.DeleteConfigRequest;
import org.hypertrace.config.service.v1.GetAllConfigsRequest;
import org.hypertrace.config.service.v1.UpsertConfigRequest;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpaceConfigRequestConverterImplTest {
  private static final String TEST_ID = "test-id";
  private static final String TEST_KEY = "test-key";
  private static final String TEST_SCOPE = "test-scope";

  private static final SpaceConfigRule RULE =
      SpaceConfigRule.newBuilder()
          .setId(TEST_ID)
          .setAttributeValueRuleData(
              AttributeValueRuleData.newBuilder()
                  .setAttributeKey(TEST_KEY)
                  .setAttributeScope(TEST_SCOPE))
          .build();

  private static final Value VALUE =
      Value.newBuilder()
          .setStructValue(
              Struct.newBuilder()
                  .putAllFields(
                      Map.of(
                          "id",
                          Value.newBuilder().setStringValue(TEST_ID).build(),
                          "attributeValueRuleData",
                          Value.newBuilder()
                              .setStructValue(
                                  Struct.newBuilder()
                                      .putFields(
                                          "attributeKey",
                                          Value.newBuilder().setStringValue(TEST_KEY).build())
                                      .putFields(
                                          "attributeScope",
                                          Value.newBuilder().setStringValue(TEST_SCOPE).build())
                                      .build())
                              .build()))
                  .build())
          .build();

  @Mock SpaceConfigRuleIdGenerator mockIdGenerator;
  private SpaceConfigRequestConverter converter;

  @BeforeEach
  void beforeEach() {
    this.converter = new SpaceConfigRequestConverterImpl(this.mockIdGenerator);
  }

  @Test
  void convertsCreateRequest() {

    when(this.mockIdGenerator.generateId()).thenReturn(TEST_ID);

    UpsertConfigRequest convertedRequest =
        this.converter.convertCreateRequest(
            CreateRuleRequest.newBuilder()
                .setAttributeValueRuleData(RULE.getAttributeValueRuleData())
                .build());

    assertEquals(VALUE, convertedRequest.getConfig());
    assertEquals(RESOURCE_NAME, convertedRequest.getResourceName());
    assertEquals(RESOURCE_NAMESPACE, convertedRequest.getResourceNamespace());
    assertEquals(TEST_ID, convertedRequest.getContext());
  }

  @Test
  void convertsUpdateRequest() {
    UpsertConfigRequest convertedRequest =
        this.converter.convertUpdateRequest(
            UpdateRuleRequest.newBuilder().setUpdatedRule(RULE).build());

    assertEquals(VALUE, convertedRequest.getConfig());
    assertEquals(RESOURCE_NAME, convertedRequest.getResourceName());
    assertEquals(RESOURCE_NAMESPACE, convertedRequest.getResourceNamespace());
    assertEquals(TEST_ID, convertedRequest.getContext());
  }

  @Test
  void convertsGetRequest() {
    GetAllConfigsRequest convertedRequest =
        this.converter.convertGetRequest(GetRulesRequest.getDefaultInstance());

    assertEquals(RESOURCE_NAME, convertedRequest.getResourceName());
    assertEquals(RESOURCE_NAMESPACE, convertedRequest.getResourceNamespace());
  }

  @Test
  void convertsDeleteRequest() {
    DeleteConfigRequest convertedRequest =
        this.converter.convertDeleteRequest(DeleteRuleRequest.newBuilder().setId(TEST_ID).build());

    assertEquals(RESOURCE_NAME, convertedRequest.getResourceName());
    assertEquals(RESOURCE_NAMESPACE, convertedRequest.getResourceNamespace());
    assertEquals(TEST_ID, convertedRequest.getContext());
  }
}
