package org.hypertrace.core.attribute.service.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.List;
import org.hypertrace.core.attribute.service.v1.AggregateFunction;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeType;

/** Utility class with helper methods for Aggregate functions of Attributes */
public class AggregateFunctionUtil {
  @VisibleForTesting
  public static List<AggregateFunction> DEFAULT_AGG_FUNCS_FOR_METRICS =
      List.of(
          AggregateFunction.SUM,
          AggregateFunction.MIN,
          AggregateFunction.MAX,
          AggregateFunction.AVG,
          AggregateFunction.AVGRATE,
          AggregateFunction.PERCENTILE);

  @VisibleForTesting
  public static List<AggregateFunction> DEFAULT_AGG_FUNCS_FOR_NUMERIC_ATTRS =
      List.of(
          AggregateFunction.SUM,
          AggregateFunction.MIN,
          AggregateFunction.MAX,
          AggregateFunction.AVG);

  @VisibleForTesting
  public static List<AggregateFunction> DEFAULT_AGG_FUNCS_FOR_STRING_ATTRS =
      List.of(AggregateFunction.DISTINCT_COUNT);

  public static List<AggregateFunction> getDefaultAggregateFunctionsByTypeAndKind(
      AttributeType attributeType, AttributeKind attributeValueKind) {
    if (attributeType.equals(AttributeType.METRIC)) {
      return DEFAULT_AGG_FUNCS_FOR_METRICS;
    } else {
      return getDefaultAggregateFunctionsByAttributeKind(attributeValueKind);
    }
  }

  private static List<AggregateFunction> getDefaultAggregateFunctionsByAttributeKind(
      AttributeKind attributeKind) {
    switch (attributeKind) {
      case TYPE_DOUBLE:
      case TYPE_INT64:
        return DEFAULT_AGG_FUNCS_FOR_NUMERIC_ATTRS;
      case TYPE_STRING:
        return DEFAULT_AGG_FUNCS_FOR_STRING_ATTRS;
      default:
        return Collections.emptyList();
    }
  }
}
