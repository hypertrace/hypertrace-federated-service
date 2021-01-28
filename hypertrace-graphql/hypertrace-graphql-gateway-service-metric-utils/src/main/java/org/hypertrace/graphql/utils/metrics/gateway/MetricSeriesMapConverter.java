package org.hypertrace.graphql.utils.metrics.gateway;

import static org.hypertrace.core.graphql.common.utils.CollectorUtils.immutableMapEntryCollector;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.gateway.service.v1.common.MetricSeries;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.MetricInterval;

class MetricSeriesMapConverter
    implements BiConverter<
        List<MetricSeriesRequest>, Map<String, MetricSeries>, Map<Duration, List<MetricInterval>>> {

  private final MetricIntervalListConverter intervalListConverter;

  @Inject
  MetricSeriesMapConverter(MetricIntervalListConverter intervalListConverter) {
    this.intervalListConverter = intervalListConverter;
  }

  @Override
  public Single<Map<Duration, List<MetricInterval>>> convert(
      List<MetricSeriesRequest> seriesRequests, Map<String, MetricSeries> resultMap) {
    return Observable.fromIterable(seriesRequests)
        .groupBy(MetricSeriesRequest::period)
        .flatMapSingle(
            groupedRequests ->
                this.buildMetricSeriesEntry(groupedRequests.getKey(), groupedRequests, resultMap))
        .collect(immutableMapEntryCollector());
  }

  private Single<Entry<Duration, List<MetricInterval>>> buildMetricSeriesEntry(
      Duration period,
      Observable<MetricSeriesRequest> seriesRequestsForDuration,
      Map<String, MetricSeries> resultMap) {
    return seriesRequestsForDuration
        .map(request -> Map.entry(request, resultMap.get(request.alias())))
        .collect(CollectorUtils.immutableMapEntryCollector())
        .flatMap(this.intervalListConverter::convert)
        .map(intervalList -> Map.entry(period, intervalList));
  }
}
