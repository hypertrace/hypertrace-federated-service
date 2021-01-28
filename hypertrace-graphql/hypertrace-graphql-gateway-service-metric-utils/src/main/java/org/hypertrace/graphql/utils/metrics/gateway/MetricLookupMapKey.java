package org.hypertrace.graphql.utils.metrics.gateway;

import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.AVGRATE;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.PERCENTILE;

import java.time.Duration;
import java.util.List;
import lombok.Value;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricArguments;

@Value
class MetricLookupMapKey {
  AttributeModelMetricAggregationType aggregationType;
  List<Object> arguments;

  public static MetricLookupMapKey basicAggregation(
      AttributeModelMetricAggregationType aggregationType) {
    return new MetricLookupMapKey(aggregationType, List.of());
  }

  public static MetricLookupMapKey avgRateAggregation(Duration duration) {
    return new MetricLookupMapKey(AVGRATE, MetricArguments.avgRateWithPeriod(duration));
  }

  public static MetricLookupMapKey percentileAggregation(int value) {
    return new MetricLookupMapKey(PERCENTILE, MetricArguments.percentileWithSize(value));
  }

  public static MetricLookupMapKey fromAggregationRequest(MetricAggregationRequest request) {
    return new MetricLookupMapKey(request.aggregation(), request.arguments());
  }
}
