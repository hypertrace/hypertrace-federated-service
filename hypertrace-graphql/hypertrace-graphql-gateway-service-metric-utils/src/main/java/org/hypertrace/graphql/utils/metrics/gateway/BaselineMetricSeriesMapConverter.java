package org.hypertrace.graphql.utils.metrics.gateway;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.gateway.service.v1.baseline.BaselineMetricSeries;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.BaselinedMetricInterval;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.hypertrace.core.graphql.common.utils.CollectorUtils.immutableMapEntryCollector;

class BaselineMetricSeriesMapConverter
    implements BiConverter<
        List<MetricSeriesRequest>,
        Map<String, BaselineMetricSeries>,
        Map<Duration, List<BaselinedMetricInterval>>> {
  private final BaselineMetricIntervalListContainer intervalListConverter;

  @Inject
  BaselineMetricSeriesMapConverter(BaselineMetricIntervalListContainer intervalListConverter) {
    this.intervalListConverter = intervalListConverter;
  }

  @Override
  public Single<Map<Duration, List<BaselinedMetricInterval>>> convert(
      List<MetricSeriesRequest> metricSeriesRequests,
      Map<String, BaselineMetricSeries> baselineMetricSeriesMap) {
    return Observable.fromIterable(metricSeriesRequests)
        .groupBy(MetricSeriesRequest::period)
        .flatMapSingle(
            groupedRequests ->
                this.buildMetricSeriesEntry(
                    groupedRequests.getKey(), groupedRequests, baselineMetricSeriesMap))
        .collect(immutableMapEntryCollector());
  }

  private Single<Map.Entry<Duration, List<BaselinedMetricInterval>>> buildMetricSeriesEntry(
      Duration period,
      Observable<MetricSeriesRequest> seriesRequestsForDuration,
      Map<String, BaselineMetricSeries> baselineSeriesMap) {
    return seriesRequestsForDuration
        .map(request -> Map.entry(request, getBaselineSeries(baselineSeriesMap, request)))
        .collect(CollectorUtils.immutableMapEntryCollector())
        .flatMap(this.intervalListConverter::convert)
        .map(intervalList -> Map.entry(period, intervalList));
  }

  private BaselineMetricSeries getBaselineSeries(
      Map<String, BaselineMetricSeries> baselineSeriesMap, MetricSeriesRequest request) {
    if (baselineSeriesMap.containsKey(request.alias())) {
      return baselineSeriesMap.get(request.alias());
    }
    return BaselineMetricSeries.getDefaultInstance();
  }
}
