package org.hypertrace.core.attribute.service.projection.functions;

import static java.util.Objects.nonNull;

import javax.annotation.Nullable;

public class DefaultValue {

  @Nullable
  public static String defaultString(@Nullable String value, @Nullable String defaultValue) {
    return nonNull(value) && !value.isEmpty() ? value : defaultValue;
  }
}
