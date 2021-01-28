package org.hypertrace.core.attribute.service.projection;

import static org.hypertrace.core.attribute.service.projection.AttributeKindWithNullability.nonNullableKind;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.doubleLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.longLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.stringLiteral;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_INT64;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class UnaryAttributeProjectionTest {

  private final UnaryAttributeProjection<Long, Long> incrementProjection =
      new UnaryAttributeProjection<>(
          nonNullableKind(TYPE_INT64), nonNullableKind(TYPE_INT64), x -> x + 1);

  @Test
  void projectsForAnyConvertibleArgTypes() {
    assertEquals(longLiteral(5), incrementProjection.project(List.of(longLiteral(4))));
    assertEquals(longLiteral(6), incrementProjection.project(List.of(doubleLiteral(5.0d))));
    assertEquals(longLiteral(7), incrementProjection.project(List.of(stringLiteral("6"))));
  }
}
