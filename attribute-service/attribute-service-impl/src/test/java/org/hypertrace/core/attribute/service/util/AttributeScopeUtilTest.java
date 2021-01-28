package org.hypertrace.core.attribute.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.junit.jupiter.api.Test;

class AttributeScopeUtilTest {

  @Test
  void resolvesScopeStringFromAttribute() {
    // Nothing set
    assertEquals(
        AttributeScope.SCOPE_UNDEFINED.name(),
        AttributeScopeUtil.resolveScopeString((AttributeMetadata) null));
    assertEquals(
        AttributeScope.SCOPE_UNDEFINED.name(),
        AttributeScopeUtil.resolveScopeString(AttributeMetadata.getDefaultInstance()));
    // Just scope set
    assertEquals(
        AttributeScope.TRACE.name(),
        AttributeScopeUtil.resolveScopeString(
            AttributeMetadata.newBuilder().setScope(AttributeScope.TRACE).build()));

    // String should be source of truth
    assertEquals(
        "OTHER_SCOPE",
        AttributeScopeUtil.resolveScopeString(
            AttributeMetadata.newBuilder()
                .setScope(AttributeScope.TRACE)
                .setScopeString("OTHER_SCOPE")
                .build()));
    // String only
    assertEquals(
        "OTHER_SCOPE",
        AttributeScopeUtil.resolveScopeString(
            AttributeMetadata.newBuilder().setScopeString("OTHER_SCOPE").build()));
  }

  @Test
  void resolveScopeStringFromScope() {
    assertEquals(
        AttributeScope.TRACE.name(), AttributeScopeUtil.resolveScopeString(AttributeScope.TRACE));

    assertEquals(
        AttributeScope.SCOPE_UNDEFINED.name(),
        AttributeScopeUtil.resolveScopeString((AttributeScope) null));
  }

  @Test
  void resolveScopeFromString() {
    assertEquals(AttributeScope.SCOPE_UNDEFINED, AttributeScopeUtil.resolveScope(null));
    assertEquals(AttributeScope.SCOPE_UNDEFINED, AttributeScopeUtil.resolveScope("OTHER"));
    assertEquals(
        AttributeScope.TRACE, AttributeScopeUtil.resolveScope(AttributeScope.TRACE.name()));
  }
}
