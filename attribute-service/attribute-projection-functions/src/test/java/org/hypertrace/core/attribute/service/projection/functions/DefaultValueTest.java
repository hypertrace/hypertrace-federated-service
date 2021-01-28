package org.hypertrace.core.attribute.service.projection.functions;

import static org.hypertrace.core.attribute.service.projection.functions.DefaultValue.defaultString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class DefaultValueTest {
  @Test
  void givesDefaultIfStringValueNullOrEmpty() {
    assertEquals("default", defaultString(null, "default"));
    assertEquals("default", defaultString("", "default"));
    assertEquals("foo", defaultString("foo", "default"));
  }

  @Test
  void returnsNullIfNullDefaultGiven() {
    assertNull(defaultString(null, null));
  }
}
