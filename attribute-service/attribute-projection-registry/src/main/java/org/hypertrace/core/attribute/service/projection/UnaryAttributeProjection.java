package org.hypertrace.core.attribute.service.projection;

import java.util.List;
import java.util.function.Function;

class UnaryAttributeProjection<T, R> extends AbstractAttributeProjection<R> {
  private final Function<T, R> projectionImplementation;

  UnaryAttributeProjection(
      AttributeKindWithNullability resultKind,
      AttributeKindWithNullability argumentKind,
      Function<T, R> projectionImplementation) {
    super(resultKind, List.of(argumentKind));
    this.projectionImplementation = projectionImplementation;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected R doUnwrappedProjection(List<Object> arguments) {
    return this.projectionImplementation.apply((T) arguments.get(0));
  }
}
