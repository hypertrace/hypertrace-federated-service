package org.hypertrace.core.attribute.service.cachingclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttributeCacheContextKeyTest {

  @Mock RequestContext mockRequestContext;

  @Test
  void matchesAnotherContextWithSameTenant() {
    RequestContext matchingContext = mock(RequestContext.class);
    when(this.mockRequestContext.getTenantId()).thenReturn(Optional.of("tenant id 1"));
    when(matchingContext.getTenantId()).thenReturn(Optional.of("tenant id 1"));
    assertEquals(
        AttributeCacheContextKey.forContext(this.mockRequestContext),
        AttributeCacheContextKey.forContext(matchingContext));
    verify(mockRequestContext, never()).getRequestHeaders();
  }

  @Test
  void doesNotMatchContextFromDifferentTenant() {
    RequestContext matchingContext = mock(RequestContext.class);
    when(this.mockRequestContext.getTenantId()).thenReturn(Optional.of("tenant id 1"));
    when(matchingContext.getTenantId()).thenReturn(Optional.of("tenant id 2"));
    assertNotEquals(
        AttributeCacheContextKey.forContext(this.mockRequestContext),
        AttributeCacheContextKey.forContext(matchingContext));
  }
}
