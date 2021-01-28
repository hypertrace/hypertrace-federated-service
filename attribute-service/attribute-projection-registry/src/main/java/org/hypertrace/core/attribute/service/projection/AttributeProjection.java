package org.hypertrace.core.attribute.service.projection;

import java.util.List;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.LiteralValue;

public interface AttributeProjection {
  /**
   * Performs the projection operation with the provided arguments.
   *
   * @param arguments to the projection
   * @return the result of the projection
   * @throws IllegalArgumentException if the provided arguments do not match the expected arity of
   *     the projection, can not be converted to the expected input types or produce a result that
   *     can't be converted to the expected output type.
   */
  LiteralValue project(List<LiteralValue> arguments);

  AttributeKind getResultKind();

  List<AttributeKind> getArgumentKinds();
}
