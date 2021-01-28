package org.hypertrace.core.attribute.service;

import org.hypertrace.core.attribute.service.v1.AttributeCreateRequest;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.attribute.service.v1.AttributeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AttributeMetadataValidatorTest {

  @Test
  public void testAttributeCreateRequestValid() {
    AttributeCreateRequest attributeCreateRequest =
        AttributeCreateRequest.newBuilder()
            .addAttributes(
                AttributeMetadata.newBuilder()
                    .setScope(AttributeScope.EVENT)
                    .setKey("name")
                    .setFqn("EVENT.name")
                    .setValueKind(AttributeKind.TYPE_STRING)
                    .setType(AttributeType.ATTRIBUTE)
                    .build())
            .build();
    AttributeMetadataValidator.validate(attributeCreateRequest);
  }

  @Test
  public void testAttributeMetadataValidatorInvalidAttributeMetadata() {
    // Don't set Scope and verify there is a validation error
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          AttributeCreateRequest attributeCreateRequest =
              AttributeCreateRequest.newBuilder()
                  .addAttributes(
                      AttributeMetadata.newBuilder()
                          .setKey("name")
                          .setFqn("EVENT.name")
                          .setValueKind(AttributeKind.TYPE_STRING)
                          .setType(AttributeType.ATTRIBUTE)
                          .build())
                  .build();
          AttributeMetadataValidator.validate(attributeCreateRequest);
        });
  }
}
