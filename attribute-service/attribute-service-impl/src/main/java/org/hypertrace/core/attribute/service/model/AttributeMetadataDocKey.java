package org.hypertrace.core.attribute.service.model;

import static org.hypertrace.core.attribute.service.util.AttributeScopeUtil.resolveScopeString;

import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.documentstore.Key;

/** Used to get the document for an AttributeMetadata document */
public class AttributeMetadataDocKey implements Key {

  private static final String SEPARATOR = ":";

  private final String tenantId;
  private final String attributeScope;
  private final String name;

  public AttributeMetadataDocKey(String tenantId, String attributeScope, String name) {
    this.tenantId = tenantId;
    this.attributeScope = attributeScope;
    this.name = name;
  }

  public static AttributeMetadataDocKey from(String tenantId, AttributeMetadata attributeMetadata) {
    return new AttributeMetadataDocKey(
        tenantId, resolveScopeString(attributeMetadata), attributeMetadata.getKey());
  }

  @Override
  public String toString() {
    return tenantId + SEPARATOR + attributeScope + SEPARATOR + name;
  }
}
