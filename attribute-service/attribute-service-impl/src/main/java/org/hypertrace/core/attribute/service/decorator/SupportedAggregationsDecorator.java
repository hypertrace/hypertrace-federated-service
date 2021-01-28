package org.hypertrace.core.attribute.service.decorator;

import static org.hypertrace.core.attribute.service.util.AggregateFunctionUtil.getDefaultAggregateFunctionsByTypeAndKind;

import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata.Builder;
import org.hypertrace.core.attribute.service.v1.AttributeType;

/**
 * Decorates the AttributeMetadata builder with default supported aggregations based on the {@link
 * AttributeType attribute type} and {@link AttributeKind attribute value kind}, if not overridden
 * explicitly
 */
public class SupportedAggregationsDecorator {
  private final Builder attributeMetadataBuilder;

  public SupportedAggregationsDecorator(Builder attributeMetadataBuilder) {
    this.attributeMetadataBuilder = attributeMetadataBuilder;
  }

  public Builder decorate() {
    // If AttributeMetadata already has list of supported aggregations,
    // it indicates they were overridden and needs to be given preference
    if (attributeMetadataBuilder.getSupportedAggregationsCount() > 0) {
      return attributeMetadataBuilder;
    }
    return attributeMetadataBuilder.addAllSupportedAggregations(
        getDefaultAggregateFunctionsByTypeAndKind(
            attributeMetadataBuilder.getType(), attributeMetadataBuilder.getValueKind()));
  }
}
