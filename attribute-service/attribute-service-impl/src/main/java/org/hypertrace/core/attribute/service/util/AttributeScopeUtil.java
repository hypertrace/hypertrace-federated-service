package org.hypertrace.core.attribute.service.util;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeScope;

public class AttributeScopeUtil {

  @Nonnull
  public static String resolveScopeString(@Nullable AttributeMetadata metadata) {
    if (isNull(metadata)) {
      return resolveScopeString(AttributeScope.SCOPE_UNDEFINED);
    }
    return resolveScopeString(metadata.getScope(), metadata.getScopeString());
  }

  @Nonnull
  public static String resolveScopeString(@Nullable AttributeScope scope) {
    return resolveScopeString(scope, null);
  }

  @Nonnull
  private static String resolveScopeString(
      @Nullable AttributeScope scope, @Nullable String scopeString) {
    if (!isNullOrEmpty(scopeString)) {
      return scopeString;
    }
    if (nonNull(scope) && scope != AttributeScope.UNRECOGNIZED) {
      return scope.name();
    }
    return AttributeScope.SCOPE_UNDEFINED.name();
  }

  @Nonnull
  public static AttributeScope resolveScope(@Nullable String scopeString) {
    try {
      return AttributeScope.valueOf(scopeString);
    } catch (Throwable ignored) {
      return AttributeScope.SCOPE_UNDEFINED;
    }
  }
}
