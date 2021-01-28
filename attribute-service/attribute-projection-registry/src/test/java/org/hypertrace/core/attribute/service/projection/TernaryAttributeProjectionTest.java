package org.hypertrace.core.attribute.service.projection;

import static org.hypertrace.core.attribute.service.projection.AttributeKindWithNullability.nonNullableKind;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.booleanLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.doubleLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.longLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.stringLiteral;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_BOOL;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_INT64;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

public class TernaryAttributeProjectionTest {
  private final TernaryAttributeProjection<Long, Long, Long, Long> sumProjection =
      new TernaryAttributeProjection<>(
          nonNullableKind(TYPE_INT64),
          nonNullableKind(TYPE_INT64),
          nonNullableKind(TYPE_INT64),
          nonNullableKind(TYPE_INT64),
          TernaryAttributeProjectionTest::ternarySum);

  @Test
  void projectsForAnyConvertibleArgTypes() {
    assertEquals(
        longLiteral(15),
        sumProjection.project(List.of(longLiteral(4), stringLiteral("1"), longLiteral(10))));
    assertEquals(
        longLiteral(17),
        sumProjection.project(List.of(doubleLiteral(5.0d), longLiteral(1), doubleLiteral(11.0d))));
    assertEquals(
        longLiteral(19),
        sumProjection.project(List.of(stringLiteral("3"), doubleLiteral(4.0f), longLiteral(12))));
  }

  @Test
  void throwsIfArgListIsOfUnexpectedLength() {
    assertThrows(IllegalArgumentException.class, () -> sumProjection.project(List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> sumProjection.project(List.of(longLiteral(1), longLiteral(2))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            sumProjection.project(
                List.of(longLiteral(1), longLiteral(2), longLiteral(3), longLiteral(4))));
  }

  @Test
  void throwsIfAnyArgIsNotConvertible() {
    assertThrows(
        IllegalArgumentException.class,
        () -> sumProjection.project(List.of(booleanLiteral(true), longLiteral(1), longLiteral(2))));
    assertThrows(
        IllegalArgumentException.class,
        () -> sumProjection.project(List.of(longLiteral(1), booleanLiteral(true), longLiteral(1))));
    assertThrows(
        IllegalArgumentException.class,
        () -> sumProjection.project(List.of(longLiteral(1), longLiteral(2), booleanLiteral(true))));
  }

  @Test
  void throwsIfResultIsNotConvertibleToExpectedType() {
    TernaryAttributeProjection<Long, Long, Long, Long> badProjection =
        new TernaryAttributeProjection<>(
            nonNullableKind(TYPE_BOOL),
            nonNullableKind(TYPE_INT64),
            nonNullableKind(TYPE_INT64),
            nonNullableKind(TYPE_INT64),
            TernaryAttributeProjectionTest::ternarySum);

    assertThrows(
        UnsupportedOperationException.class,
        () -> badProjection.project(List.of(longLiteral(2), longLiteral(1), longLiteral(3))));
  }

  private static Long ternarySum(Long x, Long y, Long z) {
    return x + y + z;
  }
}
