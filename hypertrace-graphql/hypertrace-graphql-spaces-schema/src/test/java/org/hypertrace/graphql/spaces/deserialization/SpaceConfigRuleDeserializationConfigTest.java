package org.hypertrace.graphql.spaces.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.multibindings.Multibinder;
import java.util.Map;
import org.hypertrace.core.graphql.common.schema.id.Identifiable;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.deserialization.GraphQlDeserializationRegistryModule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleAttributeValueRule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleDefinition;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpaceConfigRuleDeserializationConfigTest {

  private ArgumentDeserializer argumentDeserializer;

  @BeforeEach
  void beforeEach() {
    this.argumentDeserializer =
        Guice.createInjector(
                new GraphQlDeserializationRegistryModule(),
                new AbstractModule() {
                  @Override
                  protected void configure() {
                    Multibinder.newSetBinder(binder(), ArgumentDeserializationConfig.class)
                        .addBinding()
                        .to(SpaceConfigRuleDeserializationConfig.class);
                  }
                })
            .getInstance(ArgumentDeserializer.class);
  }

  @Test
  void testDeserialization() {
    Map<String, Object> argMap =
        Map.of(
            SpaceConfigRule.ARGUMENT_NAME,
            Map.of(
                SpaceConfigRuleDefinition.SPACE_CONFIG_RULE_DEFINITION_TYPE_KEY,
                "ATTRIBUTE_VALUE",
                Identifiable.IDENTITY_FIELD_NAME,
                "rule-id",
                SpaceConfigRuleDefinition.SPACE_CONFIG_RULE_DEFINITION_ATTRIBUTE_VALUE_RULE_KEY,
                Map.of(
                    SpaceConfigRuleAttributeValueRule.ATTRIBUTE_VALUE_ATTRIBUTE_KEY_KEY,
                    "key",
                    SpaceConfigRuleAttributeValueRule.ATTRIBUTE_VALUE_ATTRIBUTE_SCOPE_KEY,
                    "scope")));

    SpaceConfigRule result =
        this.argumentDeserializer.deserializeObject(argMap, SpaceConfigRule.class).orElseThrow();

    assertEquals("rule-id", result.id());
    assertEquals(SpaceConfigRuleType.ATTRIBUTE_VALUE, result.type());
    assertEquals("scope", result.attributeValueRule().attributeScope());
    assertEquals("key", result.attributeValueRule().attributeKey());
  }
}
