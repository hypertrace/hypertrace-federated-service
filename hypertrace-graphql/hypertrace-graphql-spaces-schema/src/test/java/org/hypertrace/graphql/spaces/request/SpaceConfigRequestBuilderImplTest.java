package org.hypertrace.graphql.spaces.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.hypertrace.core.graphql.common.utils.attributes.AttributeScopeStringTranslator;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.graphql.spaces.deserialization.SpaceRuleIdArgument;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleDefinition;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpaceConfigRequestBuilderImplTest {
  private static final String INTERNAL_SCOPE = "internal_scope";
  private static final String EXTERNAL_SCOPE = "external_scope";

  @Mock AttributeScopeStringTranslator mockScopeTranslator;
  @Mock ArgumentDeserializer mockArgumentDeserializer;
  @Mock GraphQlRequestContext mockContext;
  @Mock Map<String, Object> mockArguments;

  SpaceConfigRequestBuilderImpl requestBuilder;

  @BeforeEach
  void beforeEach() {
    this.requestBuilder =
        new SpaceConfigRequestBuilderImpl(this.mockArgumentDeserializer, this.mockScopeTranslator);
  }

  @Test
  void testBuildCreateRequest() {
    SpaceConfigRuleDefinition definitionArg =
        mock(SpaceConfigRuleDefinition.class, RETURNS_DEEP_STUBS);

    when(this.mockScopeTranslator.fromExternal(EXTERNAL_SCOPE)).thenReturn(INTERNAL_SCOPE);
    when(definitionArg.type()).thenReturn(SpaceConfigRuleType.ATTRIBUTE_VALUE);
    when(definitionArg.attributeValueRule().attributeScope()).thenReturn(EXTERNAL_SCOPE);
    when(definitionArg.attributeValueRule().attributeKey()).thenReturn("key");

    when(this.mockArgumentDeserializer.deserializeObject(
            this.mockArguments, SpaceConfigRuleDefinition.class))
        .thenReturn(Optional.of(definitionArg));

    SpaceConfigRuleCreationRequest request =
        this.requestBuilder.buildCreationRequest(mockContext, mockArguments);

    assertSame(this.mockContext, request.context());
    assertEquals(SpaceConfigRuleType.ATTRIBUTE_VALUE, request.ruleDefinition().type());
    assertEquals("key", request.ruleDefinition().attributeValueRule().attributeKey());
    assertEquals(INTERNAL_SCOPE, request.ruleDefinition().attributeValueRule().attributeScope());
  }

  @Test
  void testBuildUpdateRequest() {
    SpaceConfigRule ruleArg = mock(SpaceConfigRule.class, RETURNS_DEEP_STUBS);

    when(this.mockScopeTranslator.fromExternal(EXTERNAL_SCOPE)).thenReturn(INTERNAL_SCOPE);
    when(ruleArg.type()).thenReturn(SpaceConfigRuleType.ATTRIBUTE_VALUE);
    when(ruleArg.attributeValueRule().attributeScope()).thenReturn(EXTERNAL_SCOPE);
    when(ruleArg.attributeValueRule().attributeKey()).thenReturn("key");
    when(ruleArg.id()).thenReturn("rule-id");

    when(this.mockArgumentDeserializer.deserializeObject(this.mockArguments, SpaceConfigRule.class))
        .thenReturn(Optional.of(ruleArg));

    SpaceConfigRuleUpdateRequest request =
        this.requestBuilder.buildUpdateRequest(mockContext, mockArguments);

    assertSame(this.mockContext, request.context());
    assertEquals("rule-id", request.rule().id());
    assertEquals(SpaceConfigRuleType.ATTRIBUTE_VALUE, request.rule().type());
    assertEquals("key", request.rule().attributeValueRule().attributeKey());
    assertEquals(INTERNAL_SCOPE, request.rule().attributeValueRule().attributeScope());
  }

  @Test
  void testBuildDeleteRequest() {
    when(this.mockArgumentDeserializer.deserializePrimitive(
            this.mockArguments, SpaceRuleIdArgument.class))
        .thenReturn(Optional.of("rule-id"));

    SpaceConfigRuleDeleteRequest request =
        this.requestBuilder.buildDeleteRequest(mockContext, mockArguments);

    assertSame(this.mockContext, request.context());
    assertEquals("rule-id", request.id());
  }
}
