package org.hypertrace.graphql.utils.metrics.gateway;

import io.reactivex.rxjava3.core.Single;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.graphql.metric.schema.MetricAggregation;

import javax.inject.Inject;

class MetricAggregationConverter implements Converter<Value, MetricAggregation> {

  private final Converter<Value, Object> valueConverter;

  @Inject
  MetricAggregationConverter(Converter<Value, Object> valueConverter) {
    this.valueConverter = valueConverter;
  }

  @Override
  public Single<MetricAggregation> convert(Value value) {
    if (Value.getDefaultInstance().equals(value)) {
      return Single.just(ConvertedMetricAggregation.EMPTY);
    }

    return this.valueConverter
        .convert(value)
        .cast(Number.class)
        .map(Number::doubleValue)
        .map(ConvertedMetricAggregation::new);
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedMetricAggregation implements MetricAggregation {
    static final ConvertedMetricAggregation EMPTY = new ConvertedMetricAggregation(null);

    Double value;
  }
}
