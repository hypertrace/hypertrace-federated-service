package org.hypertrace.graphql.utils.metrics.gateway;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observables.GroupedObservable;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.baseline.Baseline;
import org.hypertrace.gateway.service.v1.baseline.BaselineInterval;
import org.hypertrace.gateway.service.v1.baseline.BaselineMetricSeries;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.BaselinedMetricInterval;
import org.hypertrace.graphql.metric.schema.MetricBaselineAggregation;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BaselineMetricIntervalListContainer
    implements Converter<
        Map<MetricSeriesRequest, BaselineMetricSeries>, List<BaselinedMetricInterval>> {

  private final MetricBaselineAggregationConverter baselineAggregationConverter;

  @Inject
  BaselineMetricIntervalListContainer(
          MetricBaselineAggregationConverter baselineAggregationConverter) {
    this.baselineAggregationConverter = baselineAggregationConverter;
  }

  @Override
  public Single<List<BaselinedMetricInterval>> convert(
      Map<MetricSeriesRequest, BaselineMetricSeries> metricSeriesMap) {
    return this.collectPartialsFromEachSeries(metricSeriesMap)
        .groupBy(BaselineMetricIntervalListContainer.MergeableIntervalPartial::getIntervalTimeRange)
        .flatMapSingle(this::buildMetricIntervalFromPartials)
        .sorted(Comparator.comparing(BaselinedMetricInterval::startTime))
        .collect(Collectors.toUnmodifiableList());
  }

  private Single<BaselinedMetricInterval> buildMetricIntervalFromPartials(
      GroupedObservable<IntervalTimeRange, MergeableIntervalPartial> partialsForTimeRange) {
    return partialsForTimeRange
        .distinct()
        .collect(
            Collectors.groupingBy(
                partial ->
                    MetricLookupMapKey.fromAggregationRequest(partial.getAggregationRequest()),
                Collectors.mapping(
                    BaselineMetricIntervalListContainer.MergeableIntervalPartial::getAggregation,
                    CollectorUtils.firstOrDefault(null))))
        .map(
            aggregationMap ->
                new BaselineMetricIntervalListContainer.ConvertedMetricInterval(
                    aggregationMap, partialsForTimeRange.getKey()));
  }

  private Observable<BaselineMetricIntervalListContainer.MergeableIntervalPartial>
      collectPartialsFromEachSeries(
          Map<MetricSeriesRequest, BaselineMetricSeries> metricSeriesMap) {
    return Observable.fromIterable(metricSeriesMap.entrySet())
        .flatMapStream(this::flattenSeriesEntry)
        .flatMapSingle(entry -> this.buildMergeablePartial(entry.getValue(), entry.getKey()));
  }

  private Stream<Map.Entry<MetricAggregationRequest, BaselineInterval>> flattenSeriesEntry(
      Map.Entry<MetricSeriesRequest, BaselineMetricSeries> entry) {
    return entry.getValue().getBaselineValueList().stream()
        .map(interval -> Map.entry(entry.getKey().aggregationRequest(), interval));
  }

  private Single<BaselineMetricIntervalListContainer.MergeableIntervalPartial>
      buildMergeablePartial(
          BaselineInterval interval, MetricAggregationRequest aggregationRequest) {
    return this.baselineAggregationConverter
        .convert(interval.getBaseline())
        .map(
            aggregation ->
                new BaselineMetricIntervalListContainer.MergeableIntervalPartial(
                    this.extractIntervalTimeRange(interval), aggregationRequest, aggregation));
  }

  private BaselineMetricIntervalListContainer.IntervalTimeRange extractIntervalTimeRange(
      BaselineInterval interval) {
    return new BaselineMetricIntervalListContainer.IntervalTimeRange(
        Instant.ofEpochMilli(interval.getStartTimeMillis()),
        Instant.ofEpochMilli(interval.getEndTimeMillis()),
        getMetricBaselineAggregation(interval.getBaseline()));
  }

  private MetricBaselineAggregation getMetricBaselineAggregation(Baseline baseline) {
    if (Baseline.getDefaultInstance().equals(baseline)) {
      return MetricBaselineAggregationConverter.MetricBaselineAggregationImpl.EMPTY;
    }
    return new MetricBaselineAggregationConverter.MetricBaselineAggregationImpl(
        baseline.getValue().getDouble(),
        baseline.getLowerBound().getDouble(),
        baseline.getUpperBound().getDouble());
  }

  @Value
  private static class IntervalTimeRange {
    Instant startTime;
    Instant endTime;
    MetricBaselineAggregation metricBaselineAggregation;
  }

  @Value
  private static class MergeableIntervalPartial {
    BaselineMetricIntervalListContainer.IntervalTimeRange intervalTimeRange;
    MetricAggregationRequest aggregationRequest;
    MetricBaselineAggregation aggregation;
  }

  @Getter
  @Accessors(fluent = true)
  private static class ConvertedMetricInterval extends MetricBaselineConvertedAggregationContainer
      implements BaselinedMetricInterval {
    final Instant startTime;
    final Instant endTime;

    ConvertedMetricInterval(
        Map<MetricLookupMapKey, MetricBaselineAggregation> metricAggregationMap,
        IntervalTimeRange intervalTimeRange) {
      super(metricAggregationMap);
      this.startTime = intervalTimeRange.getStartTime();
      this.endTime = intervalTimeRange.getEndTime();
    }
  }
}
