package org.hypertrace.core.attribute.service.cachingclient;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.hypertrace.core.grpcutils.client.rx.GrpcRxExecutionContext;
import org.hypertrace.core.grpcutils.context.RequestContext;

class AttributeCacheContextKey {
  static AttributeCacheContextKey forCurrentContext() {
    return forContext(RequestContext.CURRENT.get());
  }

  static AttributeCacheContextKey forContext(RequestContext context) {
    return new AttributeCacheContextKey(Objects.requireNonNull(context));
  }

  private static final String DEFAULT_IDENTITY = "default";

  private final GrpcRxExecutionContext executionContext;
  private final String identity;

  private AttributeCacheContextKey(@Nonnull RequestContext requestContext) {
    this.executionContext = GrpcRxExecutionContext.forContext(requestContext);
    this.identity = requestContext.getTenantId().orElse(DEFAULT_IDENTITY);
  }

  public GrpcRxExecutionContext getExecutionContext() {
    return executionContext;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AttributeCacheContextKey that = (AttributeCacheContextKey) o;
    return identity.equals(that.identity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identity);
  }
}
