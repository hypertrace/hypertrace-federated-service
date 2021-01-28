package org.hypertrace.graphql.utils.metrics.gateway;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observables.GroupedObservable;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Interval;
import org.hypertrace.gateway.service.v1.common.MetricSeries;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.MetricAggregation;
import org.hypertrace.graphql.metric.schema.MetricInterval;

class MetricIntervalListConverter
    implements Converter<Map<MetricSeriesRequest, MetricSeries>, List<MetricInterval>> {

  private final MetricAggregationConverter aggregationConverter;

  @Inject
  MetricIntervalListConverter(MetricAggregationConverter aggregationConverter) {
    this.aggregationConverter = aggregationConverter;
  }

  @Override
  public Single<List<MetricInterval>> convert(
      Map<MetricSeriesRequest, MetricSeries> metricSeriesMap) {
    return this.collectPartialsFromEachSeries(metricSeriesMap)
        .groupBy(MergeableIntervalPartial::getIntervalTimeRange)
        .flatMapSingle(this::buildMetricIntervalFromPartials)
        .sorted(Comparator.comparing(MetricInterval::startTime))
        .collect(Collectors.toUnmodifiableList());
  }

  private Single<MetricInterval> buildMetricIntervalFromPartials(
      GroupedObservable<IntervalTimeRange, MergeableIntervalPartial> partialsForTimeRange) {
    return partialsForTimeRange
        .distinct()
        .collect(
            Collectors.groupingBy(
                partial ->
                    MetricLookupMapKey.fromAggregationRequest(partial.getAggregationRequest()),
                Collectors.mapping(
                    MergeableIntervalPartial::getAggregation, CollectorUtils.firstOrDefault(null))))
        .map(
            aggregationMap ->
                new ConvertedMetricInterval(aggregationMap, partialsForTimeRange.getKey()));
  }

  private Observable<MergeableIntervalPartial> collectPartialsFromEachSeries(
      Map<MetricSeriesRequest, MetricSeries> metricSeriesMap) {
    return Observable.fromIterable(metricSeriesMap.entrySet())
        .flatMapStream(this::flattenSeriesEntry)
        .flatMapSingle(entry -> this.buildMergeablePartial(entry.getValue(), entry.getKey()));
  }

  private Stream<Entry<MetricAggregationRequest, Interval>> flattenSeriesEntry(
      Entry<MetricSeriesRequest, MetricSeries> entry) {
    return entry.getValue().getValueList().stream()
        .map(interval -> Map.entry(entry.getKey().aggregationRequest(), interval));
  }

  private Single<MergeableIntervalPartial> buildMergeablePartial(
      Interval interval, MetricAggregationRequest aggregationRequest) {
    return this.aggregationConverter
        .convert(interval.getValue())
        .map(
            aggregation ->
                new MergeableIntervalPartial(
                    this.extractIntervalTimeRange(interval), aggregationRequest, aggregation));
  }

  private IntervalTimeRange extractIntervalTimeRange(Interval interval) {
    return new IntervalTimeRange(
        Instant.ofEpochMilli(interval.getStartTimeMillis()),
        Instant.ofEpochMilli(interval.getEndTimeMillis()));
  }

  @Value
  private static class IntervalTimeRange {
    Instant startTime;
    Instant endTime;
  }

  @Value
  private static class MergeableIntervalPartial {
    IntervalTimeRange intervalTimeRange;
    MetricAggregationRequest aggregationRequest;
    MetricAggregation aggregation;
  }

  @Getter
  @Accessors(fluent = true)
  private static class ConvertedMetricInterval extends ConvertedAggregationContainer
      implements MetricInterval {
    final Instant startTime;
    final Instant endTime;

    ConvertedMetricInterval(
        Map<MetricLookupMapKey, MetricAggregation> metricAggregationMap,
        IntervalTimeRange intervalTimeRange) {
      super(metricAggregationMap);
      this.startTime = intervalTimeRange.getStartTime();
      this.endTime = intervalTimeRange.getEndTime();
    }
  }
}
