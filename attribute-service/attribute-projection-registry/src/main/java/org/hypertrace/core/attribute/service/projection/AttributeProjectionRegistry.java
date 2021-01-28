package org.hypertrace.core.attribute.service.projection;

import static org.hypertrace.core.attribute.service.projection.AttributeKindWithNullability.nonNullableKind;
import static org.hypertrace.core.attribute.service.projection.AttributeKindWithNullability.nullableKind;
import static org.hypertrace.core.attribute.service.v1.AttributeKind.*;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_CONCAT;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_CONDITIONAL;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_HASH;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_STRING_EQUALS;

import java.util.Map;
import java.util.Optional;
import org.hypertrace.core.attribute.service.projection.functions.Concatenate;
import org.hypertrace.core.attribute.service.projection.functions.Conditional;
import org.hypertrace.core.attribute.service.projection.functions.Equals;
import org.hypertrace.core.attribute.service.projection.functions.Hash;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.ProjectionOperator;

public class AttributeProjectionRegistry {

  private static final Map<ProjectionOperator, AttributeProjection> PROJECTION_MAP =
      Map.of(
          PROJECTION_OPERATOR_CONCAT,
          new BinaryAttributeProjection<>(
              nullableKind(TYPE_STRING),
              nullableKind(TYPE_STRING),
              nullableKind(TYPE_STRING),
              Concatenate::concatenate),
          PROJECTION_OPERATOR_HASH,
          new UnaryAttributeProjection<>(
              nullableKind(TYPE_STRING), nullableKind(TYPE_STRING), Hash::hash),
          PROJECTION_OPERATOR_STRING_EQUALS,
          new BinaryAttributeProjection<>(
              nonNullableKind(TYPE_BOOL),
              nullableKind(TYPE_STRING),
              nullableKind(TYPE_STRING),
              Equals::stringEquals),
          PROJECTION_OPERATOR_CONDITIONAL,
          new TernaryAttributeProjection<>(
              nullableKind(TYPE_STRING),
              nullableKind(TYPE_BOOL),
              nullableKind(TYPE_STRING),
              nullableKind(TYPE_STRING),
              Conditional::getValue
          ));

  public Optional<AttributeProjection> getProjection(ProjectionOperator projectionOperator) {
    return Optional.ofNullable(PROJECTION_MAP.get(projectionOperator));
  }
}
