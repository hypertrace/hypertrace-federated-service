package org.hypertrace.core.attribute.service.decorator;

import java.util.List;
import org.hypertrace.core.attribute.service.util.AggregateFunctionUtil;
import org.hypertrace.core.attribute.service.v1.AggregateFunction;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SupportedAggregationsDecoratorTest {

  @Test
  public void testDecoratorForTypeMetric() {
    AttributeMetadata attributeMetadata =
        new SupportedAggregationsDecorator(
                AttributeMetadata.newBuilder().setType(AttributeType.METRIC))
            .decorate()
            .build();
    Assertions.assertTrue(
        attributeMetadata
            .getSupportedAggregationsList()
            .containsAll(AggregateFunctionUtil.DEFAULT_AGG_FUNCS_FOR_METRICS));
  }

  @Test
  public void testDecoratorForTypeAttribute() {
    {
      AttributeMetadata attributeMetadata =
          new SupportedAggregationsDecorator(
                  AttributeMetadata.newBuilder()
                      .setType(AttributeType.ATTRIBUTE)
                      .setValueKind(AttributeKind.TYPE_INT64))
              .decorate()
              .build();
      Assertions.assertTrue(
          attributeMetadata
              .getSupportedAggregationsList()
              .containsAll(AggregateFunctionUtil.DEFAULT_AGG_FUNCS_FOR_NUMERIC_ATTRS));
    }

    {
      AttributeMetadata attributeMetadata =
          new SupportedAggregationsDecorator(
                  AttributeMetadata.newBuilder()
                      .setType(AttributeType.ATTRIBUTE)
                      .setValueKind(AttributeKind.TYPE_DOUBLE))
              .decorate()
              .build();
      Assertions.assertTrue(
          attributeMetadata
              .getSupportedAggregationsList()
              .containsAll(AggregateFunctionUtil.DEFAULT_AGG_FUNCS_FOR_NUMERIC_ATTRS));
    }

    {
      AttributeMetadata attributeMetadata =
          new SupportedAggregationsDecorator(
                  AttributeMetadata.newBuilder()
                      .setType(AttributeType.ATTRIBUTE)
                      .setValueKind(AttributeKind.TYPE_STRING))
              .decorate()
              .build();
      Assertions.assertTrue(
          attributeMetadata
              .getSupportedAggregationsList()
              .containsAll(AggregateFunctionUtil.DEFAULT_AGG_FUNCS_FOR_STRING_ATTRS));
    }
  }

  @Test
  public void testDecoratorOverridden() {
    List<AggregateFunction> aggregateFunctions =
        List.of(AggregateFunction.COUNT, AggregateFunction.AVGRATE);
    AttributeMetadata attributeMetadata =
        new SupportedAggregationsDecorator(
                AttributeMetadata.newBuilder()
                    .setType(AttributeType.ATTRIBUTE)
                    .setValueKind(AttributeKind.TYPE_INT64)
                    .addAllSupportedAggregations(aggregateFunctions))
            .decorate()
            .build();
    Assertions.assertEquals(
        aggregateFunctions.size(), attributeMetadata.getSupportedAggregationsCount());
    Assertions.assertTrue(
        attributeMetadata.getSupportedAggregationsList().containsAll(aggregateFunctions));
  }
}
