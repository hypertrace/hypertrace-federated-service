package org.hypertrace.core.attribute.service.projection.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ConcatenateTest {

  @Test
  void concatenatesNormalStrings() {
    assertEquals("foobar", Concatenate.concatenate("foo", "bar"));
    assertEquals("foo", Concatenate.concatenate("foo", ""));
    assertEquals("bar", Concatenate.concatenate("", "bar"));
  }

  @Test
  void concatenatesNullStrings() {
    assertEquals("foo", Concatenate.concatenate("foo", null));
    assertEquals("bar", Concatenate.concatenate(null, "bar"));
    assertNull(Concatenate.concatenate(null, null));
  }
}
