package org.hypertrace.graphql.utils.metrics.gateway;

import static io.reactivex.rxjava3.core.Single.zip;
import static org.hypertrace.core.graphql.common.utils.CollectorUtils.immutableMapEntryCollector;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observables.GroupedObservable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.core.graphql.common.utils.TriConverter;
import org.hypertrace.gateway.service.v1.baseline.BaselineEntity;
import org.hypertrace.gateway.service.v1.entity.Entity;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.BaselinedMetricAggregation;
import org.hypertrace.graphql.metric.schema.BaselinedMetricInterval;
import org.hypertrace.graphql.metric.schema.MetricContainer;
import org.hypertrace.graphql.metric.schema.MetricInterval;

class MetricContainerMapConverter
    implements TriConverter<Collection<MetricRequest>, Entity, BaselineEntity,  Map<String, MetricContainer>> {

  private final BaselinedMetricAggregationMapConverter aggregationMapConverter;
  private final MetricSeriesMapConverter seriesMapConverter;
  private final BaselineMetricSeriesMapConverter baselineSeriesConverter;

  @Inject
  MetricContainerMapConverter(
      BaselinedMetricAggregationMapConverter aggregationMapConverter,
      MetricSeriesMapConverter seriesMapConverter,
      BaselineMetricSeriesMapConverter baselineSeriesConverter) {
    this.aggregationMapConverter = aggregationMapConverter;
    this.seriesMapConverter = seriesMapConverter;
    this.baselineSeriesConverter = baselineSeriesConverter;
  }

  @Override
  public Single<Map<String, MetricContainer>> convert(
      Collection<MetricRequest> metricRequests,
      Entity entity,
      BaselineEntity baselineEntity) {
    return Observable.fromIterable(metricRequests)
        .distinct()
        .groupBy(MetricRequest::attribute)
        .flatMapSingle(requests -> this.buildMetricContainerEntry(requests, entity, baselineEntity))
        .collect(immutableMapEntryCollector());
  }

  private Single<Entry<String, MetricContainer>> buildMetricContainerEntry(
      GroupedObservable<AttributeModel, MetricRequest> requestsForAttribute,
      Entity entity,
      BaselineEntity baselineEntity) {
    return requestsForAttribute
        .collect(Collectors.toUnmodifiableList())
        .flatMap(
            metricRequests ->
                this.buildMetricContainerForAttribute(metricRequests, entity, baselineEntity))
        .map(container -> Map.entry(requestsForAttribute.getKey().key(), container));
  }

  private Single<MetricContainer> buildMetricContainerForAttribute(
      List<MetricRequest> metricRequests, Entity entity, BaselineEntity baselineEntity) {
    List<MetricAggregationRequest> aggregationRequests =
        metricRequests.stream().collect(CollectorUtils.flatten(MetricRequest::aggregationRequests));
    List<MetricSeriesRequest> seriesRequests =
        metricRequests.stream().collect(CollectorUtils.flatten(MetricRequest::seriesRequests));
    List<MetricSeriesRequest> baselineSeriesRequests =
        metricRequests.stream()
            .collect(CollectorUtils.flatten(MetricRequest::baselineSeriesRequests));
    return zip(
        this.aggregationMapConverter.convert(
            aggregationRequests,
            entity.getMetricMap(),
            baselineEntity.getBaselineAggregateMetricMap()),
        this.seriesMapConverter.convert(seriesRequests, entity.getMetricSeriesMap()),
        this.baselineSeriesConverter.convert(
            baselineSeriesRequests, baselineEntity.getBaselineMetricSeriesMap()),
        BaselinedConvertedAggregationContainerImpl::new);
  }

  private static class BaselinedConvertedAggregationContainerImpl extends BaselinedConvertedAggregationContainer
      implements MetricContainer {

    private final Map<Duration, List<MetricInterval>> metricSeriesMap;
    private final Map<Duration, List<BaselinedMetricInterval>> baselineSeriesMap;

    BaselinedConvertedAggregationContainerImpl(
        Map<MetricLookupMapKey, BaselinedMetricAggregation> metricAggregationMap,
        Map<Duration, List<MetricInterval>> metricSeriesMap,
        Map<Duration, List<BaselinedMetricInterval>> baselineSeriesMap) {
      super(metricAggregationMap);
      this.metricSeriesMap = metricSeriesMap;
      this.baselineSeriesMap = baselineSeriesMap;
    }

    @Override
    public List<MetricInterval> series(int size, TimeUnit units) {
      return this.metricSeriesMap.get(Duration.of(size, units.getChronoUnit()));
    }

    @Override
    public List<BaselinedMetricInterval> baselineSeries(int size, TimeUnit units) {
      return this.baselineSeriesMap.get(Duration.of(size, units.getChronoUnit()));
    }
  }
}
