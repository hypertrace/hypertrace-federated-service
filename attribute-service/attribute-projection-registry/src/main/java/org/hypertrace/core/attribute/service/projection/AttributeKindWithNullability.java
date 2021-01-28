package org.hypertrace.core.attribute.service.projection;

import org.hypertrace.core.attribute.service.v1.AttributeKind;

class AttributeKindWithNullability {

  static AttributeKindWithNullability nonNullableKind(AttributeKind attributeKind) {
    return new AttributeKindWithNullability(attributeKind, false);
  }

  static AttributeKindWithNullability nullableKind(AttributeKind attributeKind) {
    return new AttributeKindWithNullability(attributeKind, true);
  }

  private final boolean nullable;
  private final AttributeKind kind;

  private AttributeKindWithNullability(AttributeKind kind, boolean nullable) {
    this.nullable = nullable;
    this.kind = kind;
  }

  public boolean isNullable() {
    return this.nullable;
  }

  public AttributeKind getKind() {
    return this.kind;
  }

  @Override
  public String toString() {
    return "AttributeKindWithNullability{" +
        "nullable=" + nullable +
        ", kind=" + kind +
        '}';
  }
}
