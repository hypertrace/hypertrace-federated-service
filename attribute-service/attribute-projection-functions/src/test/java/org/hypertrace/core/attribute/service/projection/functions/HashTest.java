package org.hypertrace.core.attribute.service.projection.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class HashTest {

  @Test
  void createsMatchingHashesForMatchingInputs() {
    assertEquals(Hash.hash("foo"), Hash.hash("foo"));
    assertNotEquals(Hash.hash("foo"), Hash.hash("bar"));
  }

  @Test
  void hashesNullToNull() {
    assertNull(Hash.hash(null));
  }
}
