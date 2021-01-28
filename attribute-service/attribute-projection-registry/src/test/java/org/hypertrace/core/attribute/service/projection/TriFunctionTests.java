package org.hypertrace.core.attribute.service.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class TriFunctionTests {
  @Test
  public void testApply() {
    TriFunction<Long, Long, Long, Long> tri = (x, y, z) -> x + y + z;
    Function<Long, Long> doubler = x -> 2 * x;
    assertEquals(6, tri.apply(1L, 2L, 3L));
    assertEquals(48, tri.andThen(doubler).andThen(doubler).apply(2L, 4L, 6L));
  }
}
