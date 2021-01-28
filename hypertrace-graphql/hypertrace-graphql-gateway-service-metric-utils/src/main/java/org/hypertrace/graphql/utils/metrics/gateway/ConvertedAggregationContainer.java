package org.hypertrace.graphql.utils.metrics.gateway;

import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.AVG;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.COUNT;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.DISTINCT_COUNT;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.MAX;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.MIN;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.SUM;

import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.time.Duration;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.metric.schema.MetricAggregation;
import org.hypertrace.graphql.metric.schema.MetricAggregationContainer;

@AllArgsConstructor
class ConvertedAggregationContainer implements MetricAggregationContainer {
  Map<MetricLookupMapKey, MetricAggregation> metricAggregationMap;

  @Override
  public MetricAggregation sum() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(SUM));
  }

  @Override
  public MetricAggregation min() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(MIN));
  }

  @Override
  public MetricAggregation max() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(MAX));
  }

  @Override
  public MetricAggregation avg() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(AVG));
  }

  @Override
  public MetricAggregation avgrate(int size, TimeUnit units) {
    return this.metricAggregationMap.get(
        MetricLookupMapKey.avgRateAggregation(Duration.of(size, units.getChronoUnit())));
  }

  @Override
  public MetricAggregation percentile(int size) {
    return this.metricAggregationMap.get(MetricLookupMapKey.percentileAggregation(size));
  }

  @Override
  public @GraphQLNonNull MetricAggregation count() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(COUNT));
  }

  @Override
  public @GraphQLNonNull MetricAggregation distinctcount() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(DISTINCT_COUNT));
  }
}
