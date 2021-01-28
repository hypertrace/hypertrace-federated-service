package org.hypertrace.space.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.List;
import java.util.Map;
import org.hypertrace.config.service.v1.ContextSpecificConfig;
import org.hypertrace.config.service.v1.DeleteConfigResponse;
import org.hypertrace.config.service.v1.GetAllConfigsResponse;
import org.hypertrace.config.service.v1.UpsertConfigResponse;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.DeleteRuleResponse;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class SpaceConfigResponseConverterImplTest {

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
  private SpaceConfigResponseConverter converter;

  @BeforeEach
  void beforeEach() {
    this.converter = new SpaceConfigResponseConverterImpl();
  }

  @Test
  void convertsCreateResponse() {
    assertEquals(
        RULE,
        this.converter
            .convertCreateResponse(UpsertConfigResponse.newBuilder().setConfig(VALUE).build())
            .getRule());
  }

  @Test
  void convertsUpdateResponse() {
    assertEquals(
        RULE,
        this.converter
            .convertUpdateResponse(UpsertConfigResponse.newBuilder().setConfig(VALUE).build())
            .getRule());
  }

  @Test
  void convertsGetAllResponse() {
    assertEquals(
        List.of(RULE),
        this.converter
            .convertGetResponse(
                GetAllConfigsResponse.newBuilder()
                    .addContextSpecificConfigs(
                        ContextSpecificConfig.newBuilder().setConfig(VALUE).setContext(TEST_ID))
                    .build())
            .getRulesList());
  }

  @Test
  void convertsDeleteResponse() {
    assertEquals(
        DeleteRuleResponse.getDefaultInstance(),
        this.converter.convertDeleteResponse(DeleteConfigResponse.getDefaultInstance()));
  }
}
