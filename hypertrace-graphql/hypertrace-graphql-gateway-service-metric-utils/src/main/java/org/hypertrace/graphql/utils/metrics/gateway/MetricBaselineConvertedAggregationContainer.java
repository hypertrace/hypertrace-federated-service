package org.hypertrace.graphql.utils.metrics.gateway;

import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.metric.schema.MetricBaselineAggregation;
import org.hypertrace.graphql.metric.schema.MetricBaselineAggregationContainer;

import java.time.Duration;
import java.util.Map;

import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.AVG;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.COUNT;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.DISTINCT_COUNT;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.MAX;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.MIN;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.SUM;

public class MetricBaselineConvertedAggregationContainer
    implements MetricBaselineAggregationContainer {
  Map<MetricLookupMapKey, MetricBaselineAggregation> metricAggregationMap;

  public MetricBaselineConvertedAggregationContainer(
      Map<MetricLookupMapKey, MetricBaselineAggregation> metricAggregationMap) {
    this.metricAggregationMap = metricAggregationMap;
  }

  @Override
  public MetricBaselineAggregation sum() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(SUM));
  }

  @Override
  public MetricBaselineAggregation min() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(MIN));
  }

  @Override
  public MetricBaselineAggregation max() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(MAX));
  }

  @Override
  public MetricBaselineAggregation avg() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(AVG));
  }

  @Override
  public MetricBaselineAggregation avgrate(int size, TimeUnit units) {
    return this.metricAggregationMap.get(
        MetricLookupMapKey.avgRateAggregation(Duration.of(size, units.getChronoUnit())));
  }

  @Override
  public MetricBaselineAggregation percentile(int size) {
    return this.metricAggregationMap.get(MetricLookupMapKey.percentileAggregation(size));
  }

  @Override
  public @GraphQLNonNull MetricBaselineAggregation count() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(COUNT));
  }

  @Override
  public @GraphQLNonNull MetricBaselineAggregation distinctcount() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(DISTINCT_COUNT));
  }
}
