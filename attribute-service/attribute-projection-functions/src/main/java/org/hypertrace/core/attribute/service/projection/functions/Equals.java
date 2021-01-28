package org.hypertrace.core.attribute.service.projection.functions;

import static java.util.Objects.isNull;

import javax.annotation.Nullable;

public class Equals {
  public static boolean stringEquals(@Nullable String first, @Nullable String second) {
    if (isNull(first) && isNull(second)) {
      return true;
    }
    if (isNull(first) || isNull(second)) {
      return false;
    }
    return first.equals(second);
  }
}
