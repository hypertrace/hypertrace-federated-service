package org.hypertrace.core.attribute.service.projection;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static org.hypertrace.core.attribute.service.projection.AttributeKindWithNullability.nonNullableKind;
import static org.hypertrace.core.attribute.service.projection.AttributeKindWithNullability.nullableKind;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.booleanLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.doubleLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.longLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.nullLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.stringLiteral;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_BOOL;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_INT64;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.TYPE_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class BinaryAttributeProjectionTest {

  private final BinaryAttributeProjection<Long, Long, Long> sumProjection =
      new BinaryAttributeProjection<>(
          nonNullableKind(TYPE_INT64),
          nonNullableKind(TYPE_INT64),
          nonNullableKind(TYPE_INT64),
          Long::sum);

  @Test
  void projectsForAnyConvertibleArgTypes() {
    assertEquals(
        longLiteral(5), sumProjection.project(List.of(longLiteral(4), stringLiteral("1"))));
    assertEquals(
        longLiteral(6), sumProjection.project(List.of(doubleLiteral(5.0d), longLiteral(1))));
    assertEquals(
        longLiteral(7), sumProjection.project(List.of(stringLiteral("3"), doubleLiteral(4.0f))));
  }

  @Test
  void throwsIfArgListIsOfUnexpectedLength() {
    assertThrows(IllegalArgumentException.class, () -> sumProjection.project(List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> sumProjection.project(List.of(longLiteral(1), longLiteral(2), longLiteral(3))));
  }

  @Test
  void throwsIfAnyArgIsNotConvertible() {
    assertThrows(
        IllegalArgumentException.class,
        () -> sumProjection.project(List.of(booleanLiteral(true), longLiteral(1))));
    assertThrows(
        IllegalArgumentException.class,
        () -> sumProjection.project(List.of(longLiteral(1), booleanLiteral(true))));
  }

  @Test
  void throwsIfResultIsNotConvertibleToExpectedType() {
    BinaryAttributeProjection<Long, Long, Long> badProjection =
        new BinaryAttributeProjection<>(
            nonNullableKind(TYPE_BOOL),
            nonNullableKind(TYPE_INT64),
            nonNullableKind(TYPE_INT64),
            Long::sum);

    assertThrows(
        UnsupportedOperationException.class,
        () -> badProjection.project(List.of(longLiteral(2), longLiteral(1))));
  }

  @Test
  void supportsNullableArguments() {
    BinaryAttributeProjection<String, String, String> nullableArgProjection =
        new BinaryAttributeProjection<>(
            nonNullableKind(TYPE_STRING),
            nullableKind(TYPE_STRING),
            nullableKind(TYPE_STRING),
            (first, second) ->
                requireNonNullElse(first, "firstIsNull")
                    + ":"
                    + requireNonNullElse(second, "secondIsNull"));

    assertEquals(
        stringLiteral("firstIsNull:secondIsNull"),
        nullableArgProjection.project(List.of(nullLiteral(), nullLiteral())));

    assertEquals(
        stringLiteral("foo:secondIsNull"),
        nullableArgProjection.project(List.of(stringLiteral("foo"), nullLiteral())));

    assertEquals(
        stringLiteral("firstIsNull:bar"),
        nullableArgProjection.project(List.of(nullLiteral(), stringLiteral("bar"))));

    assertEquals(
        stringLiteral("foo:bar"),
        nullableArgProjection.project(List.of(stringLiteral("foo"), stringLiteral("bar"))));
  }

  @Test
  void supportsNullableResults() {
    BinaryAttributeProjection<String, String, String> nullableArgProjection =
        new BinaryAttributeProjection<>(
            nullableKind(TYPE_STRING),
            nullableKind(TYPE_STRING),
            nullableKind(TYPE_STRING),
            (first, second) -> isNull(first) || isNull(second) ? null : first + ":" + second);

    assertEquals(
        nullLiteral(), nullableArgProjection.project(List.of(nullLiteral(), nullLiteral())));

    assertEquals(
        nullLiteral(), nullableArgProjection.project(List.of(stringLiteral("foo"), nullLiteral())));

    assertEquals(
        nullLiteral(), nullableArgProjection.project(List.of(nullLiteral(), stringLiteral("bar"))));

    assertEquals(
        stringLiteral("foo:bar"),
        nullableArgProjection.project(List.of(stringLiteral("foo"), stringLiteral("bar"))));
  }

  @Test
  void throwsIfResultIsNotConvertibleToExpectedNullableType() {
    BinaryAttributeProjection<Long, Long, Long> badProjection =
        new BinaryAttributeProjection<>(
            nullableKind(TYPE_BOOL),
            nonNullableKind(TYPE_INT64),
            nonNullableKind(TYPE_INT64),
            Long::sum);

    assertThrows(
        UnsupportedOperationException.class,
        () -> badProjection.project(List.of(longLiteral(2), longLiteral(1))));
  }
}
