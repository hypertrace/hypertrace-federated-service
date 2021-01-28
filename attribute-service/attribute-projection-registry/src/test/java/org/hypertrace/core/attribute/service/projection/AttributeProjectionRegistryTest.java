package org.hypertrace.core.attribute.service.projection;

import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_UNSET;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.UNRECOGNIZED;

import java.util.Arrays;
import org.hypertrace.core.attribute.service.v1.ProjectionOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AttributeProjectionRegistryTest {

  AttributeProjectionRegistry registry = new AttributeProjectionRegistry();

  @Test
  void supportsAllDeclaredFunctionTypes() {
    Arrays.stream(ProjectionOperator.values())
        .filter(
            projectionOperator ->
                projectionOperator != UNRECOGNIZED
                    && projectionOperator != PROJECTION_OPERATOR_UNSET)
        .forEach(
            projectionOperator ->
                Assertions.assertTrue(
                    registry.getProjection(projectionOperator).isPresent(),
                    "Projection declared but not present in registry " + projectionOperator));
  }
}
