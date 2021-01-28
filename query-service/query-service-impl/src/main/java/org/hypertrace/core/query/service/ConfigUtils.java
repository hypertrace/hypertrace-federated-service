package org.hypertrace.core.query.service;

import java.util.Optional;
import java.util.function.Supplier;

public class ConfigUtils {

  public static <T> Optional<T> optionallyGet(Supplier<T> strictGet) {
    try {
      return Optional.ofNullable(strictGet.get());
    } catch (Throwable t) {
      return Optional.empty();
    }
  }
}
