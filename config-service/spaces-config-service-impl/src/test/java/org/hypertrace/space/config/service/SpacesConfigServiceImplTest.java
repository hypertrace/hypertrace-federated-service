package org.hypertrace.space.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import org.hypertrace.config.service.MockGenericConfigService;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.SpacesConfigServiceGrpc;
import org.hypertrace.spaces.config.service.v1.SpacesConfigServiceGrpc.SpacesConfigServiceBlockingStub;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpacesConfigServiceImplTest {
  SpacesConfigServiceBlockingStub spacesStub;
  MockGenericConfigService mockGenericConfigService;

  @BeforeEach
  void beforeEach() {
    this.mockGenericConfigService =
        new MockGenericConfigService().mockUpsert().mockGetAll().mockDelete();

    this.mockGenericConfigService
        .addService(new SpacesConfigServiceImpl(this.mockGenericConfigService.channel()))
        .start();

    this.spacesStub =
        SpacesConfigServiceGrpc.newBlockingStub(this.mockGenericConfigService.channel());
  }

  @AfterEach
  void afterEach() {
    this.mockGenericConfigService.shutdown();
  }

  @Test
  void createReadUpdateReadDelete() {

    AttributeValueRuleData attributeValueRuleData1 =
        AttributeValueRuleData.newBuilder()
            .setAttributeScope("attrScope")
            .setAttributeKey("attrKey1")
            .build();

    AttributeValueRuleData attributeValueRuleData2 =
        AttributeValueRuleData.newBuilder()
            .setAttributeScope("attrScope")
            .setAttributeKey("attrKey2")
            .build();

    SpaceConfigRule createdRule1 =
        this.spacesStub
            .createRule(
                CreateRuleRequest.newBuilder()
                    .setAttributeValueRuleData(attributeValueRuleData1)
                    .build())
            .getRule();

    assertEquals(attributeValueRuleData1, createdRule1.getAttributeValueRuleData());
    assertFalse(createdRule1.getId().isEmpty());

    SpaceConfigRule createdRule2 =
        this.spacesStub
            .createRule(
                CreateRuleRequest.newBuilder()
                    .setAttributeValueRuleData(attributeValueRuleData2)
                    .build())
            .getRule();

    assertIterableEquals(
        List.of(createdRule1, createdRule2),
        this.spacesStub.getRules(GetRulesRequest.getDefaultInstance()).getRulesList());

    SpaceConfigRule ruleToUpdate =
        createdRule1.toBuilder()
            .setAttributeValueRuleData(
                attributeValueRuleData1.toBuilder().setAttributeKey("updatedAttrKey1"))
            .build();

    SpaceConfigRule updatedRule1 =
        this.spacesStub
            .updateRule(UpdateRuleRequest.newBuilder().setUpdatedRule(ruleToUpdate).build())
            .getRule();

    assertEquals(ruleToUpdate, updatedRule1);

    assertIterableEquals(
        List.of(updatedRule1, createdRule2),
        this.spacesStub.getRules(GetRulesRequest.getDefaultInstance()).getRulesList());

    this.spacesStub.deleteRule(DeleteRuleRequest.newBuilder().setId(createdRule2.getId()).build());

    assertIterableEquals(
        List.of(updatedRule1),
        this.spacesStub.getRules(GetRulesRequest.getDefaultInstance()).getRulesList());
  }
}
