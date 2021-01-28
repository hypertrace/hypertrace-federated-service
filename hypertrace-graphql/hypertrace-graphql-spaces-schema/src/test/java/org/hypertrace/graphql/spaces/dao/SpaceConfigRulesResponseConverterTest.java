package org.hypertrace.graphql.spaces.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.hypertrace.core.graphql.common.utils.attributes.AttributeScopeStringTranslator;
import org.hypertrace.graphql.spaces.schema.query.SpaceConfigRuleResultSet;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleType;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.CreateRuleResponse;
import org.hypertrace.spaces.config.service.v1.GetRulesResponse;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.UpdateRuleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpaceConfigRulesResponseConverterTest {

  private static final String INTERNAL_SCOPE = "internal_scope";
  private static final String EXTERNAL_SCOPE = "external_scope";

  @Mock AttributeScopeStringTranslator mockScopeTranslator;

  private SpaceConfigRulesResponseConverter converter;

  @BeforeEach
  void beforeEach() {
    when(this.mockScopeTranslator.toExternal(INTERNAL_SCOPE)).thenReturn(EXTERNAL_SCOPE);
    this.converter = new SpaceConfigRulesResponseConverter(this.mockScopeTranslator);
  }

  @Test
  void testConvertingGetResponse() {
    GetRulesResponse response =
        GetRulesResponse.newBuilder()
            .addRules(
                SpaceConfigRule.newBuilder()
                    .setId("id-1")
                    .setAttributeValueRuleData(
                        AttributeValueRuleData.newBuilder()
                            .setAttributeKey("key-1")
                            .setAttributeScope(INTERNAL_SCOPE)))
            .addRules(
                SpaceConfigRule.newBuilder()
                    .setId("id-2")
                    .setAttributeValueRuleData(
                        AttributeValueRuleData.newBuilder()
                            .setAttributeKey("key-2")
                            .setAttributeScope(INTERNAL_SCOPE)))
            .build();

    SpaceConfigRuleResultSet ruleResultSet =
        this.converter.convertGetResponse(response).blockingGet();

    assertEquals(2, ruleResultSet.results().size());
    assertEquals(2, ruleResultSet.count());
    assertEquals(2, ruleResultSet.total());

    assertEquals("id-1", ruleResultSet.results().get(0).id());
    assertEquals(SpaceConfigRuleType.ATTRIBUTE_VALUE, ruleResultSet.results().get(0).type());
    assertEquals("key-1", ruleResultSet.results().get(0).attributeValueRule().attributeKey());
    assertEquals(
        EXTERNAL_SCOPE, ruleResultSet.results().get(0).attributeValueRule().attributeScope());

    assertEquals("id-2", ruleResultSet.results().get(1).id());
    assertEquals(SpaceConfigRuleType.ATTRIBUTE_VALUE, ruleResultSet.results().get(1).type());
    assertEquals("key-2", ruleResultSet.results().get(1).attributeValueRule().attributeKey());
    assertEquals(
        EXTERNAL_SCOPE, ruleResultSet.results().get(1).attributeValueRule().attributeScope());
  }

  @Test
  void testConvertingCreateResponse() {
    CreateRuleResponse response =
        CreateRuleResponse.newBuilder()
            .setRule(
                SpaceConfigRule.newBuilder()
                    .setId("id-1")
                    .setAttributeValueRuleData(
                        AttributeValueRuleData.newBuilder()
                            .setAttributeScope(INTERNAL_SCOPE)
                            .setAttributeKey("key-1")))
            .build();

    org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule rule =
        this.converter.convertRule(response).blockingGet();

    assertEquals("id-1", rule.id());
    assertEquals(SpaceConfigRuleType.ATTRIBUTE_VALUE, rule.type());
    assertEquals("key-1", rule.attributeValueRule().attributeKey());
    assertEquals(EXTERNAL_SCOPE, rule.attributeValueRule().attributeScope());
  }

  @Test
  void convertsUpdateResponse() {
    UpdateRuleResponse response =
        UpdateRuleResponse.newBuilder()
            .setRule(
                SpaceConfigRule.newBuilder()
                    .setId("id-1")
                    .setAttributeValueRuleData(
                        AttributeValueRuleData.newBuilder()
                            .setAttributeScope(INTERNAL_SCOPE)
                            .setAttributeKey("key-1")))
            .build();

    org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule rule =
        this.converter.convertRule(response).blockingGet();

    assertEquals("id-1", rule.id());
    assertEquals(SpaceConfigRuleType.ATTRIBUTE_VALUE, rule.type());
    assertEquals("key-1", rule.attributeValueRule().attributeKey());
    assertEquals(EXTERNAL_SCOPE, rule.attributeValueRule().attributeScope());
  }
}
