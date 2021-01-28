package org.hypertrace.space.config.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.Map;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.SpaceConfigRule;
import org.junit.jupiter.api.Test;

class SpaceConfigRuleConverterImplTest {

  private static final SpaceConfigRuleConverterImpl CONVERTER = new SpaceConfigRuleConverterImpl();

  private static final SpaceConfigRule RULE =
      SpaceConfigRule.newBuilder()
          .setId("test-id")
          .setAttributeValueRuleData(
              AttributeValueRuleData.newBuilder()
                  .setAttributeKey("attrKey")
                  .setAttributeScope("attrScope"))
          .build();

  private static final Value VALUE =
      Value.newBuilder()
          .setStructValue(
              Struct.newBuilder()
                  .putAllFields(
                      Map.of(
                          "id",
                          Value.newBuilder().setStringValue("test-id").build(),
                          "attributeValueRuleData",
                          Value.newBuilder()
                              .setStructValue(
                                  Struct.newBuilder()
                                      .putFields(
                                          "attributeKey",
                                          Value.newBuilder().setStringValue("attrKey").build())
                                      .putFields(
                                          "attributeScope",
                                          Value.newBuilder().setStringValue("attrScope").build())
                                      .build())
                              .build()))
                  .build())
          .build();

  @Test
  void convertsFromRule() {
    assertEquals(VALUE, CONVERTER.convertToGeneric(RULE));
  }

  @Test
  void convertsToRule() {
    assertEquals(RULE, CONVERTER.convertFromGeneric(VALUE));
  }

  @Test
  void errorsOnInvalidValue() {
    // Proto are pretty resilient - missing fields are defaulted, extra fields are ignored. To break
    // it, we change the field's type here
    Value mismatchedValue =
        Value.newBuilder()
            .setStructValue(
                Struct.newBuilder()
                    .putFields(
                        "id",
                        Value.newBuilder().setListValue(ListValue.getDefaultInstance()).build()))
            .build();
    assertThrows(RuntimeException.class, () -> CONVERTER.convertFromGeneric(mismatchedValue));
  }
}
