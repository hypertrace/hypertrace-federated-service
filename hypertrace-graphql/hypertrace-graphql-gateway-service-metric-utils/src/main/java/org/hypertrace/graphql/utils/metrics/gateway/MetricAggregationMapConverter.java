package org.hypertrace.graphql.utils.metrics.gateway;

import static io.reactivex.rxjava3.core.Single.zip;
import static org.hypertrace.core.graphql.common.utils.CollectorUtils.immutableMapEntryCollector;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;

import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.utils.TriConverter;
import org.hypertrace.gateway.service.v1.baseline.Baseline;
import org.hypertrace.gateway.service.v1.common.AggregatedMetricValue;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.schema.BaselinedMetricAggregation;
import org.hypertrace.graphql.metric.schema.MetricBaselineAggregation;

class BaselinedMetricAggregationMapConverter
    implements TriConverter<
        List<MetricAggregationRequest>,
        Map<String, AggregatedMetricValue>,
        Map<String, Baseline>,
        Map<MetricLookupMapKey, BaselinedMetricAggregation>> {

  private final MetricAggregationConverter aggregationConverter;
  private final MetricBaselineAggregationConverter baselineAggregationConverter;

  @Inject
  BaselinedMetricAggregationMapConverter(
      MetricAggregationConverter aggregationConverter,
      MetricBaselineAggregationConverter baselineAggregationConverter) {
    this.aggregationConverter = aggregationConverter;
    this.baselineAggregationConverter = baselineAggregationConverter;
  }

  @Override
  public Single<Map<MetricLookupMapKey, BaselinedMetricAggregation>> convert(
      List<MetricAggregationRequest> aggregationRequests,
      Map<String, AggregatedMetricValue> resultMap,
      Map<String, Baseline> baselineMap) {
    return Observable.fromIterable(aggregationRequests)
        .flatMapSingle(
            aggregationRequest ->
                this.buildMetricAggregationEntry(aggregationRequest, resultMap, baselineMap))
        .collect(immutableMapEntryCollector());
  }

  private Single<Entry<MetricLookupMapKey, BaselinedMetricAggregation>> buildMetricAggregationEntry(
      MetricAggregationRequest aggregationRequest,
      Map<String, AggregatedMetricValue> resultMap,
      Map<String, Baseline> baselineMap) {
    return zip(
            this.aggregationConverter.convert(resultMap.get(aggregationRequest.alias()).getValue()),
            this.baselineAggregationConverter.convert(getBaseline(aggregationRequest, baselineMap)),
            (metricAggregation, metricBaselineAggregation) ->
                new BaselinedMetricAggregationImpl(
                    metricAggregation.value(), metricBaselineAggregation))
        .map(agg -> Map.entry(MetricLookupMapKey.fromAggregationRequest(aggregationRequest), agg));
  }

  private Baseline getBaseline(
      MetricAggregationRequest aggregationRequest, Map<String, Baseline> baselineMap) {
    return baselineMap.containsKey(aggregationRequest.alias())
        ? baselineMap.get(aggregationRequest.alias())
        : Baseline.getDefaultInstance();
  }

  @Value
  @Accessors(fluent = true)
  private static class BaselinedMetricAggregationImpl implements BaselinedMetricAggregation {
    Double metricAggregationValue;
    MetricBaselineAggregation metricBaselineAggregation;

    @Override
    public MetricBaselineAggregation baseline() {
      return metricBaselineAggregation;
    }

    @Override
    public Double value() {
      return metricAggregationValue;
    }
  }
}
