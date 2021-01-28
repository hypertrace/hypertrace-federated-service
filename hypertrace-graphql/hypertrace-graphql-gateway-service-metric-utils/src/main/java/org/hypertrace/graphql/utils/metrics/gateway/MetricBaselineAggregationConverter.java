package org.hypertrace.graphql.utils.metrics.gateway;

import io.reactivex.rxjava3.core.Single;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.baseline.Baseline;
import org.hypertrace.graphql.metric.schema.MetricBaselineAggregation;

class MetricBaselineAggregationConverter implements Converter<Baseline, MetricBaselineAggregation> {

  @Override
  public Single<MetricBaselineAggregation> convert(Baseline baseline) {
    return Single.just(getBaselinedMetricAggregation(baseline));
  }

  private MetricBaselineAggregation getBaselinedMetricAggregation(Baseline baseline) {
    if (Baseline.getDefaultInstance().equals(baseline)) {
      return MetricBaselineAggregationImpl.EMPTY;
    }
    return new MetricBaselineAggregationImpl(
        baseline.getValue().getDouble(),
        baseline.getLowerBound().getDouble(),
        baseline.getUpperBound().getDouble());
  }

  @lombok.Value
  @Accessors(fluent = true)
  static class MetricBaselineAggregationImpl implements MetricBaselineAggregation {
    static final MetricBaselineAggregation EMPTY =
        new MetricBaselineAggregationImpl(null, null, null);
    Double value;
    Double lowerBound;
    Double upperBound;
  }
}
