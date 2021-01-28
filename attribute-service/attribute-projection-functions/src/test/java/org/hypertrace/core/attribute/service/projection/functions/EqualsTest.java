package org.hypertrace.core.attribute.service.projection.functions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EqualsTest {
  @Test
  public void testStringsEqual() {
    assertTrue(Equals.stringEquals("foo", "foo"));
    assertFalse(Equals.stringEquals("foo", "bar"));
    assertFalse(Equals.stringEquals("foo", null));
    assertFalse(Equals.stringEquals(null, "bar"));
    assertTrue(Equals.stringEquals(null, null));
  }
}
