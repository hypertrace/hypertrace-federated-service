package org.hypertrace.graphql.utils.metrics.gateway;

import static io.reactivex.rxjava3.core.Single.zip;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.TimeAggregation;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;

class MetricSeriesExpressionConverter
    implements Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>> {
  private final PeriodConverter periodConverter;
  private final MetricAggregationExpressionConverter aggregationExpressionConverter;

  @Inject
  MetricSeriesExpressionConverter(
      PeriodConverter periodConverter,
      MetricAggregationExpressionConverter aggregationExpressionConverter) {
    this.periodConverter = periodConverter;
    this.aggregationExpressionConverter = aggregationExpressionConverter;
  }

  @Override
  public Single<Set<TimeAggregation>> convert(Collection<MetricSeriesRequest> metricSeries) {
    return Observable.fromIterable(metricSeries)
        .flatMapSingle(this::buildSeriesExpression)
        .collect(Collectors.toUnmodifiableSet());
  }

  private Single<TimeAggregation> buildSeriesExpression(MetricSeriesRequest request) {
    return zip(
        this.aggregationExpressionConverter.convert(request.aggregationRequest(), request.alias()),
        this.periodConverter.convert(request.period()),
        (aggregation, period) ->
            TimeAggregation.newBuilder().setAggregation(aggregation).setPeriod(period).build());
  }
}
