package org.hypertrace.core.attribute.service.projection.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ConditionalTest {
  @Test
  public void testConditional() {
    assertEquals("foo", Conditional.getValue(true, "foo", "bar"));
    assertEquals("bar", Conditional.getValue(false, "foo", "bar"));
    assertNull(Conditional.getValue(null, "foo", "bar"));
  }
}
