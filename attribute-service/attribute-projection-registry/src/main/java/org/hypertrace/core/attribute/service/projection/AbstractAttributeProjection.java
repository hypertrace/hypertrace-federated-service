package org.hypertrace.core.attribute.service.projection;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.LiteralValue;

abstract class AbstractAttributeProjection<R> implements AttributeProjection {

  private final AttributeKindWithNullability resultKindWithNullability;
  private final List<AttributeKindWithNullability> argumentKindsWithNullability;

  protected AbstractAttributeProjection(
      AttributeKindWithNullability resultKind, List<AttributeKindWithNullability> argumentKinds) {
    this.resultKindWithNullability = resultKind;
    this.argumentKindsWithNullability = argumentKinds;
  }

  @Override
  public LiteralValue project(List<LiteralValue> arguments) {
    Preconditions.checkArgument(arguments.size() == this.argumentKindsWithNullability.size());
    List<Object> unwrappedArguments = new ArrayList<>(this.argumentKindsWithNullability.size());
    for (int index = 0; index < arguments.size(); index++) {
      int argumentIndex = index;
      LiteralValue argumentLiteral = arguments.get(argumentIndex);
      AttributeKindWithNullability maybeNullableKind =
          this.argumentKindsWithNullability.get(argumentIndex);
      Object unwrappedArgument =
          ValueCoercer.fromLiteral(argumentLiteral, maybeNullableKind.getKind())
              .orElseGet(
                  () -> {
                    if (maybeNullableKind.isNullable()) {
                      return null;
                    }
                    throw new IllegalArgumentException(
                        String.format(
                            "Projection argument %s at index %d could not be converted to expected type %s",
                            argumentLiteral, argumentIndex, maybeNullableKind));
                  });

      unwrappedArguments.add(argumentIndex, unwrappedArgument);
    }
    Object unwrappedResult = this.doUnwrappedProjection(unwrappedArguments);
    return ValueCoercer.toLiteral(
            unwrappedResult,
            this.resultKindWithNullability.getKind(),
            this.resultKindWithNullability.isNullable())
        .orElseThrow(
            () ->
                new UnsupportedOperationException(
                    String.format(
                        "Projection result %s could not be converted to expected type %s",
                        unwrappedResult, this.resultKindWithNullability)));
  }

  @Override
  public AttributeKind getResultKind() {
    return this.resultKindWithNullability.getKind();
  }

  @Override
  public List<AttributeKind> getArgumentKinds() {
    return this.argumentKindsWithNullability.stream()
        .map(AttributeKindWithNullability::getKind)
        .collect(Collectors.toUnmodifiableList());
  }

  protected abstract R doUnwrappedProjection(List<Object> arguments);
}
