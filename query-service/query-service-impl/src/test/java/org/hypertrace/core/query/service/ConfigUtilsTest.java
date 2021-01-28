package org.hypertrace.core.query.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConfigUtilsTest {

  @Test
  void optionallyGetReturnsEmptyIfThrows() {
    assertEquals(
        Optional.empty(),
        ConfigUtils.optionallyGet(
            () -> {
              throw new RuntimeException();
            }));
  }

  @Test
  void optionallyGetReturnsValueIfProvided() {
    assertEquals(Optional.of("value"), ConfigUtils.optionallyGet(() -> "value"));
    assertEquals(Optional.of(10), ConfigUtils.optionallyGet(() -> 10));
  }
}
