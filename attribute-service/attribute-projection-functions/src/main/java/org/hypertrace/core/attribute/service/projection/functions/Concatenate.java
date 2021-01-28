package org.hypertrace.core.attribute.service.projection.functions;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;

import javax.annotation.Nullable;

public class Concatenate {
  private static final String DEFAULT_STRING = "";

  @Nullable
  public static String concatenate(@Nullable String first, @Nullable String second) {
    if (isNull(first) && isNull(second)) {
      return null;
    }
    return requireNonNullElse(first, DEFAULT_STRING) + requireNonNullElse(second, DEFAULT_STRING);
  }
}
