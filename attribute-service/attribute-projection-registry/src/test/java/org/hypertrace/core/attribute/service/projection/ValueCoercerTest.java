package org.hypertrace.core.attribute.service.projection;

import static java.util.Optional.empty;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.booleanLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.doubleLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.fromLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.longLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.nullLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.stringLiteral;
import static org.hypertrace.core.attribute.service.projection.ValueCoercer.toLiteral;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.LiteralValue;
import org.junit.jupiter.api.Test;

class ValueCoercerTest {
  // 1600000000000 epoch => 2020-09-13T12:26:40.000Z
  private static final String TEST_TIMESTAMP_STRING = "2020-09-13T12:26:40Z";
  private static final long TEST_TIMESTAMP_MS = 1600000000000L;
  private static final Instant TEST_TIMESTAMP_INSTANT = Instant.ofEpochMilli(TEST_TIMESTAMP_MS);

  @Test
  void coercesFromNull() {
    assertEquals(empty(), toLiteral(null, AttributeKind.TYPE_STRING, false));
    assertEquals(empty(), toLiteral(null, AttributeKind.TYPE_BOOL, false));
    assertEquals(empty(), toLiteral(null, AttributeKind.TYPE_DOUBLE, false));
    assertEquals(empty(), toLiteral(null, AttributeKind.TYPE_INT64, false));
    assertEquals(Optional.of(nullLiteral()), toLiteral(null, AttributeKind.TYPE_INT64, true));
  }

  @Test
  void coercesFromString() {
    assertEquals(Optional.of(stringLiteral("test")), toLiteral("test", AttributeKind.TYPE_STRING, false));
    assertEquals(Optional.of(stringLiteral("")), toLiteral("", AttributeKind.TYPE_STRING, false));

    assertEquals(empty(), toLiteral("test", AttributeKind.TYPE_INT64, false));
    assertEquals(Optional.of(longLiteral(10)), toLiteral("10", AttributeKind.TYPE_INT64, false));
    assertEquals(Optional.of(longLiteral(-10)), toLiteral("-10", AttributeKind.TYPE_INT64, false));
    assertEquals(empty(), toLiteral("10.0", AttributeKind.TYPE_INT64, false));

    assertEquals(empty(), toLiteral("test", AttributeKind.TYPE_DOUBLE, false));
    assertEquals(Optional.of(doubleLiteral(10)), toLiteral("10", AttributeKind.TYPE_DOUBLE, false));
    assertEquals(Optional.of(doubleLiteral(-10)), toLiteral("-10", AttributeKind.TYPE_DOUBLE, false));
    assertEquals(Optional.of(doubleLiteral(10.5)), toLiteral("10.5", AttributeKind.TYPE_DOUBLE, false));

    assertEquals(empty(), toLiteral("test", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(true)), toLiteral("true", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(true)), toLiteral("TRUE", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(false)), toLiteral("fALse", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(false)), toLiteral("false", AttributeKind.TYPE_BOOL, false));

    assertEquals(empty(), toLiteral("test", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(true)), toLiteral("true", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(true)), toLiteral("TRUE", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(false)), toLiteral("fALse", AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(false)), toLiteral("false", AttributeKind.TYPE_BOOL, false));

    assertEquals(empty(), toLiteral("test", AttributeKind.TYPE_TIMESTAMP, false));
    assertEquals(
        Optional.of(longLiteral(TEST_TIMESTAMP_MS)),
        toLiteral(String.valueOf(TEST_TIMESTAMP_MS), AttributeKind.TYPE_TIMESTAMP, false));
    assertEquals(
        Optional.of(longLiteral(TEST_TIMESTAMP_MS)),
        toLiteral(TEST_TIMESTAMP_STRING, AttributeKind.TYPE_TIMESTAMP, false));
  }

  @Test
  void coercesFromBoolean() {
    assertEquals(Optional.of(booleanLiteral(true)), toLiteral(true, AttributeKind.TYPE_BOOL, false));
    assertEquals(Optional.of(booleanLiteral(false)), toLiteral(false, AttributeKind.TYPE_BOOL, false));

    assertEquals(Optional.of(stringLiteral("true")), toLiteral(true, AttributeKind.TYPE_STRING, false));
    assertEquals(Optional.of(stringLiteral("false")), toLiteral(false, AttributeKind.TYPE_STRING, false));

    assertEquals(empty(), toLiteral(true, AttributeKind.TYPE_INT64, false));
    assertEquals(empty(), toLiteral(true, AttributeKind.TYPE_DOUBLE, false));
    assertEquals(empty(), toLiteral(true, AttributeKind.TYPE_TIMESTAMP, false));
  }

  @Test
  void coercesFromLong() {
    assertEquals(Optional.of(longLiteral(10L)), toLiteral(10L, AttributeKind.TYPE_INT64, false));
    assertEquals(Optional.of(longLiteral(-10L)), toLiteral(-10L, AttributeKind.TYPE_INT64, false));

    assertEquals(Optional.of(doubleLiteral(10d)), toLiteral(10L, AttributeKind.TYPE_DOUBLE, false));
    assertEquals(Optional.of(doubleLiteral(-10d)), toLiteral(-10L, AttributeKind.TYPE_DOUBLE, false));

    assertEquals(Optional.of(stringLiteral("10")), toLiteral(10L, AttributeKind.TYPE_STRING, false));
    assertEquals(Optional.of(stringLiteral("-10")), toLiteral(-10L, AttributeKind.TYPE_STRING, false));

    assertEquals(Optional.of(longLiteral(10L)), toLiteral(10L, AttributeKind.TYPE_TIMESTAMP, false));
    assertEquals(empty(), toLiteral(-10L, AttributeKind.TYPE_TIMESTAMP, false));

    assertEquals(empty(), toLiteral(10L, AttributeKind.TYPE_BOOL, false));
  }

  @Test
  void coercesFromDouble() {
    assertEquals(Optional.of(longLiteral(10L)), toLiteral(10d, AttributeKind.TYPE_INT64, false));
    assertEquals(Optional.of(longLiteral(-10L)), toLiteral(-10d, AttributeKind.TYPE_INT64, false));

    assertEquals(Optional.of(doubleLiteral(10d)), toLiteral(10d, AttributeKind.TYPE_DOUBLE, false));
    assertEquals(Optional.of(doubleLiteral(-10d)), toLiteral(-10d, AttributeKind.TYPE_DOUBLE, false));

    assertEquals(Optional.of(stringLiteral("10.0")), toLiteral(10d, AttributeKind.TYPE_STRING, false));
    assertEquals(Optional.of(stringLiteral("-10.0")), toLiteral(-10d, AttributeKind.TYPE_STRING, false));

    assertEquals(Optional.of(longLiteral(10L)), toLiteral(10d, AttributeKind.TYPE_TIMESTAMP, false));
    assertEquals(empty(), toLiteral(-10d, AttributeKind.TYPE_TIMESTAMP, false));

    assertEquals(empty(), toLiteral(10d, AttributeKind.TYPE_BOOL, false));
  }

  @Test
  void coercesFromTemporal() {
    assertEquals(
        Optional.of(longLiteral(TEST_TIMESTAMP_MS)),
        toLiteral(TEST_TIMESTAMP_INSTANT, AttributeKind.TYPE_TIMESTAMP, false));
    assertEquals(
        Optional.of(longLiteral(TEST_TIMESTAMP_MS)),
        toLiteral(
            TEST_TIMESTAMP_INSTANT.atOffset(ZoneOffset.of("+07:00")),
            AttributeKind.TYPE_TIMESTAMP, false));

    assertEquals(
        Optional.of(longLiteral(TEST_TIMESTAMP_MS)),
        toLiteral(TEST_TIMESTAMP_INSTANT, AttributeKind.TYPE_INT64, false));
    assertEquals(
        Optional.of(doubleLiteral(TEST_TIMESTAMP_MS)),
        toLiteral(TEST_TIMESTAMP_INSTANT, AttributeKind.TYPE_DOUBLE, false));

    assertEquals(
        Optional.of(stringLiteral(TEST_TIMESTAMP_STRING)),
        toLiteral(TEST_TIMESTAMP_INSTANT, AttributeKind.TYPE_STRING, false));

    assertEquals(empty(), toLiteral(TEST_TIMESTAMP_INSTANT, AttributeKind.TYPE_BOOL, false));
  }

  @Test
  void coercesToDouble() {
    assertEquals(Optional.of(10.0d), fromLiteral(doubleLiteral(10.0d), AttributeKind.TYPE_DOUBLE));
    assertEquals(Optional.of(10.0d), fromLiteral(longLiteral(10), AttributeKind.TYPE_DOUBLE));
    assertEquals(Optional.of(10.0d), fromLiteral(stringLiteral("10"), AttributeKind.TYPE_DOUBLE));
    assertEquals(Optional.of(10.0d), fromLiteral(stringLiteral("10.0"), AttributeKind.TYPE_DOUBLE));
    assertEquals(empty(), fromLiteral(stringLiteral("test"), AttributeKind.TYPE_DOUBLE));
    assertEquals(empty(), fromLiteral(booleanLiteral(true), AttributeKind.TYPE_DOUBLE));
    assertEquals(
        empty(), fromLiteral(LiteralValue.getDefaultInstance(), AttributeKind.TYPE_DOUBLE));
  }

  @Test
  void coercesToLong() {
    assertEquals(Optional.of(10L), fromLiteral(doubleLiteral(10.0d), AttributeKind.TYPE_INT64));
    assertEquals(Optional.of(10L), fromLiteral(longLiteral(10), AttributeKind.TYPE_INT64));
    assertEquals(Optional.of(10L), fromLiteral(stringLiteral("10"), AttributeKind.TYPE_INT64));
    assertEquals(empty(), fromLiteral(stringLiteral("10.0"), AttributeKind.TYPE_INT64));
    assertEquals(empty(), fromLiteral(stringLiteral("test"), AttributeKind.TYPE_INT64));
    assertEquals(empty(), fromLiteral(booleanLiteral(true), AttributeKind.TYPE_INT64));
    assertEquals(empty(), fromLiteral(LiteralValue.getDefaultInstance(), AttributeKind.TYPE_INT64));
  }

  @Test
  void coercesToTimestamp() {
    assertEquals(
        Optional.of(TEST_TIMESTAMP_MS),
        fromLiteral(doubleLiteral(TEST_TIMESTAMP_MS), AttributeKind.TYPE_TIMESTAMP));
    assertEquals(
        Optional.of(TEST_TIMESTAMP_MS),
        fromLiteral(longLiteral(TEST_TIMESTAMP_MS), AttributeKind.TYPE_TIMESTAMP));
    assertEquals(
        Optional.of(TEST_TIMESTAMP_MS),
        fromLiteral(stringLiteral(TEST_TIMESTAMP_STRING), AttributeKind.TYPE_TIMESTAMP));
    assertEquals(
        Optional.of(TEST_TIMESTAMP_MS),
        fromLiteral(
            stringLiteral(String.valueOf(TEST_TIMESTAMP_MS)), AttributeKind.TYPE_TIMESTAMP));
    assertEquals(empty(), fromLiteral(stringLiteral("test"), AttributeKind.TYPE_TIMESTAMP));
    assertEquals(empty(), fromLiteral(booleanLiteral(true), AttributeKind.TYPE_TIMESTAMP));
    assertEquals(
        empty(), fromLiteral(LiteralValue.getDefaultInstance(), AttributeKind.TYPE_TIMESTAMP));
  }

  @Test
  void coercesToBoolean() {
    assertEquals(empty(), fromLiteral(doubleLiteral(10.0d), AttributeKind.TYPE_BOOL));
    assertEquals(empty(), fromLiteral(longLiteral(10), AttributeKind.TYPE_BOOL));
    assertEquals(Optional.of(true), fromLiteral(stringLiteral("true"), AttributeKind.TYPE_BOOL));
    assertEquals(Optional.of(false), fromLiteral(stringLiteral("false"), AttributeKind.TYPE_BOOL));
    assertEquals(empty(), fromLiteral(stringLiteral("test"), AttributeKind.TYPE_BOOL));
    assertEquals(Optional.of(true), fromLiteral(booleanLiteral(true), AttributeKind.TYPE_BOOL));
    assertEquals(Optional.of(false), fromLiteral(booleanLiteral(false), AttributeKind.TYPE_BOOL));
    assertEquals(empty(), fromLiteral(LiteralValue.getDefaultInstance(), AttributeKind.TYPE_BOOL));
  }

  @Test
  void coercesToString() {
    assertEquals(Optional.of("10.0"), fromLiteral(doubleLiteral(10.0d), AttributeKind.TYPE_STRING));
    assertEquals(Optional.of("10"), fromLiteral(longLiteral(10), AttributeKind.TYPE_STRING));
    assertEquals(
        Optional.of("true"), fromLiteral(stringLiteral("true"), AttributeKind.TYPE_STRING));
    assertEquals(Optional.of(""), fromLiteral(stringLiteral(""), AttributeKind.TYPE_STRING));
    assertEquals(Optional.of("true"), fromLiteral(booleanLiteral(true), AttributeKind.TYPE_STRING));
    assertEquals(
        empty(), fromLiteral(LiteralValue.getDefaultInstance(), AttributeKind.TYPE_STRING));
  }
}
