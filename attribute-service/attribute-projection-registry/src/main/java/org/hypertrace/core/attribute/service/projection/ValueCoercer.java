package org.hypertrace.core.attribute.service.projection;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.LiteralValue;

class ValueCoercer {

  public static Optional<?> fromLiteral(LiteralValue value, AttributeKind attributeKind) {
    switch (attributeKind) {
      case TYPE_DOUBLE:
        return extractDoubleValue(value);
      case TYPE_INT64:
        return extractLongValue(value);
      case TYPE_TIMESTAMP:
        return extractTimestamp(value);
      case TYPE_BOOL:
        return extractBooleanValue(value);
      case TYPE_STRING:
        return extractStringValue(value);
      default:
        return Optional.empty();
    }
  }

  public static Optional<LiteralValue> toLiteral(Object value, AttributeKind attributeKind, boolean nullable) {
    if (isNull(value)) {
      return nullable ? Optional.of(nullLiteral()) : Optional.empty();
    }
    if (isAssignableToAnyOfClasses(value.getClass(), CharSequence.class)) {
      return toLiteral(value.toString(), attributeKind);
    }
    if (isAssignableToAnyOfClasses(value.getClass(), Boolean.class)) {
      return toLiteral((boolean) value, attributeKind);
    }
    if (isAssignableToAnyOfClasses(
        value.getClass(),
        Long.class,
        Integer.class,
        BigInteger.class,
        Double.class,
        Float.class,
        BigDecimal.class)) {
      return toLiteral((Number) value, attributeKind);
    }
    if (isAssignableToAnyOfClasses(value.getClass(), TemporalAccessor.class)) {
      return toLiteral((TemporalAccessor) value, attributeKind);
    }
    return Optional.empty();
  }

  private static Optional<LiteralValue> toLiteral(
      @Nonnull String stringValue, AttributeKind attributeKind) {
    switch (attributeKind) {
      case TYPE_DOUBLE:
        return tryParseDouble(stringValue).map(ValueCoercer::doubleLiteral);
      case TYPE_INT64:
        return tryParseLong(stringValue).map(ValueCoercer::longLiteral);
      case TYPE_BOOL:
        return tryParseBoolean(stringValue).map(ValueCoercer::booleanLiteral);
      case TYPE_STRING:
        return Optional.of(stringLiteral(stringValue));
      case TYPE_TIMESTAMP:
        return tryParseLong(stringValue)
            .or(() -> tryParseTimestamp(stringValue))
            .map(ValueCoercer::longLiteral);
      default:
        return Optional.empty();
    }
  }

  private static Optional<LiteralValue> toLiteral(
      boolean booleanValue, AttributeKind attributeKind) {
    switch (attributeKind) {
      case TYPE_BOOL:
        return Optional.of(booleanLiteral(booleanValue));
      case TYPE_STRING:
        return Optional.of(stringLiteral(String.valueOf(booleanValue)));
      default:
        return Optional.empty();
    }
  }

  private static Optional<LiteralValue> toLiteral(
      @Nonnull TemporalAccessor temporal, AttributeKind attributeKind) {
    Instant instant = Instant.from(temporal);
    switch (attributeKind) {
      case TYPE_STRING:
        return Optional.of(stringLiteral(instant.toString()));
      case TYPE_DOUBLE:
        return Optional.of(doubleLiteral(instant.toEpochMilli()));
      case TYPE_INT64:
      case TYPE_TIMESTAMP:
        return Optional.of(longLiteral(instant.toEpochMilli()));
      default:
        return Optional.empty();
    }
  }

  private static Optional<LiteralValue> toLiteral(Number numberValue, AttributeKind attributeKind) {
    switch (attributeKind) {
      case TYPE_DOUBLE:
        return Optional.of(doubleLiteral(numberValue));
      case TYPE_INT64:
        return Optional.of(longLiteral(numberValue));
      case TYPE_TIMESTAMP:
        return numberValue.longValue() >= 0
            ? Optional.of(longLiteral(numberValue))
            : Optional.empty();
      case TYPE_STRING:
        return Optional.of(stringLiteral(String.valueOf(numberValue)));
      default:
        return Optional.empty();
    }
  }

  private static Optional<Boolean> extractBooleanValue(LiteralValue value) {
    switch (value.getValueCase()) {
      case BOOLEAN_VALUE:
        return Optional.of(value.getBooleanValue());
      case STRING_VALUE:
        return tryParseBoolean(value.getStringValue());
      default:
        return Optional.empty();
    }
  }

  private static Optional<String> extractStringValue(LiteralValue value) {
    switch (value.getValueCase()) {
      case BOOLEAN_VALUE:
        return Optional.of(String.valueOf(value.getBooleanValue()));
      case INT_VALUE:
        return Optional.of(String.valueOf(value.getIntValue()));
      case FLOAT_VALUE:
        return Optional.of(String.valueOf(value.getFloatValue()));
      case STRING_VALUE:
        return Optional.of(value.getStringValue());
      default:
        return Optional.empty();
    }
  }

  private static Optional<Long> extractLongValue(LiteralValue value) {
    switch (value.getValueCase()) {
      case FLOAT_VALUE:
        return Optional.of((long) value.getFloatValue());
      case INT_VALUE:
        return Optional.of(value.getIntValue());
      case STRING_VALUE:
        return tryParseLong(value.getStringValue());
      default:
        return Optional.empty();
    }
  }

  private static Optional<Double> extractDoubleValue(LiteralValue value) {
    switch (value.getValueCase()) {
      case FLOAT_VALUE:
        return Optional.of(value.getFloatValue());
      case INT_VALUE:
        return Optional.of((double) value.getIntValue());
      case STRING_VALUE:
        return tryParseDouble(value.getStringValue());
      default:
        return Optional.empty();
    }
  }

  private static Optional<Long> extractTimestamp(LiteralValue value) {
    return extractLongValue(value).or(() -> tryParseTimestamp(value.getStringValue()));
  }

  private static boolean isAssignableToAnyOfClasses(
      Class<?> classToCheck, Class<?>... classesAllowed) {
    for (Class<?> allowedClass : classesAllowed) {
      if (allowedClass.isAssignableFrom(classToCheck)) {
        return true;
      }
    }
    return false;
  }

  static LiteralValue stringLiteral(@Nonnull String stringValue) {
    return LiteralValue.newBuilder().setStringValue(stringValue).build();
  }

  static LiteralValue longLiteral(@Nonnull Number number) {
    return LiteralValue.newBuilder().setIntValue(number.longValue()).build();
  }

  static LiteralValue doubleLiteral(@Nonnull Number number) {
    return LiteralValue.newBuilder().setFloatValue(number.doubleValue()).build();
  }

  static LiteralValue booleanLiteral(boolean booleanValue) {
    return LiteralValue.newBuilder().setBooleanValue(booleanValue).build();
  }

  static LiteralValue nullLiteral() {
    return LiteralValue.getDefaultInstance();
  }

  private static Optional<Long> tryParseLong(@Nullable String intString) {
    try {
      return Optional.of(Long.valueOf(requireNonNull(intString)));
    } catch (Throwable ignored) {
      return Optional.empty();
    }
  }

  private static Optional<Double> tryParseDouble(@Nullable String doubleString) {
    try {
      return Optional.of(Double.valueOf(requireNonNull(doubleString)));
    } catch (Throwable ignored) {
      return Optional.empty();
    }
  }

  private static Optional<Boolean> tryParseBoolean(@Nullable String booleanString) {
    if ("true".equalsIgnoreCase(booleanString)) {
      return Optional.of(Boolean.TRUE);
    }
    if ("false".equalsIgnoreCase(booleanString)) {
      return Optional.of(Boolean.FALSE);
    }
    return Optional.empty();
  }

  private static Optional<Long> tryParseTimestamp(@Nullable String dateString) {
    try {
      return Optional.of(Instant.parse(requireNonNull(dateString))).map(Instant::toEpochMilli);
    } catch (Throwable ignored) {
      return Optional.empty();
    }
  }
}
