package org.hypertrace.graphql.utils.metrics.gateway;

import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.metric.schema.BaselinedMetricAggregation;
import org.hypertrace.graphql.metric.schema.BaselinedMetricAggregationContainer;

import java.time.Duration;
import java.util.Map;

import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.*;
import static org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType.DISTINCT_COUNT;

class BaselinedConvertedAggregationContainer implements BaselinedMetricAggregationContainer {
  Map<MetricLookupMapKey, BaselinedMetricAggregation> metricAggregationMap;

  public BaselinedConvertedAggregationContainer(Map<MetricLookupMapKey, BaselinedMetricAggregation> metricAggregationMap) {
    this.metricAggregationMap = metricAggregationMap;
  }

  @Override
  public BaselinedMetricAggregation sum() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(SUM));
  }

  @Override
  public BaselinedMetricAggregation min() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(MIN));
  }

  @Override
  public BaselinedMetricAggregation max() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(MAX));
  }

  @Override
  public BaselinedMetricAggregation avg() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(AVG));
  }

  @Override
  public BaselinedMetricAggregation avgrate(int size, TimeUnit units) {
    return this.metricAggregationMap.get(
        MetricLookupMapKey.avgRateAggregation(Duration.of(size, units.getChronoUnit())));
  }

  @Override
  public BaselinedMetricAggregation percentile(int size) {
    return this.metricAggregationMap.get(MetricLookupMapKey.percentileAggregation(size));
  }

  @Override
  public @GraphQLNonNull BaselinedMetricAggregation count() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(COUNT));
  }

  @Override
  public @GraphQLNonNull BaselinedMetricAggregation distinctcount() {
    return this.metricAggregationMap.get(MetricLookupMapKey.basicAggregation(DISTINCT_COUNT));
  }
}
