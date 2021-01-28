package org.hypertrace.core.attribute.service.projection.functions;

import javax.annotation.Nullable;

public class Conditional {
  @Nullable
  public static String getValue(@Nullable Boolean condition,
                                @Nullable String first,
                                @Nullable String second) {
    if (condition == null) {
      return null;
    }
    return condition ? first : second;
  }
}
