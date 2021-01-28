package org.hypertrace.graphql.metric.request;

import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

class DefaultMetricRequestBuilder implements MetricRequestBuilder {

  private final MetricQueryableBuilderUtil metricQueryableBuilderUtil;
  private final MetricAggregationRequestBuilder aggregationRequestBuilder;
  private final MetricSeriesRequestBuilder seriesRequestBuilder;

  @Inject
  DefaultMetricRequestBuilder(
      MetricQueryableBuilderUtil metricQueryableBuilderUtil,
      MetricAggregationRequestBuilder aggregationRequestBuilder,
      MetricSeriesRequestBuilder seriesRequestBuilder) {
    this.metricQueryableBuilderUtil = metricQueryableBuilderUtil;
    this.aggregationRequestBuilder = aggregationRequestBuilder;
    this.seriesRequestBuilder = seriesRequestBuilder;
  }

  @Override
  public Single<List<MetricRequest>> build(
      GraphQlRequestContext context,
      String requestScope,
      Stream<SelectedField> metricQueryableFieldStream) {
    return this.metricQueryableBuilderUtil.buildForEachMetricQueryable(
        context, requestScope, metricQueryableFieldStream, this::collectAggregationsAndSeries);
  }

  private Observable<MetricRequest> collectAggregationsAndSeries(
      AttributeModel attribute, SelectedField metricContainerField) {
    return Single.zip(
            this.aggregationRequestBuilder
                .build(attribute, metricContainerField)
                .collect(Collectors.toUnmodifiableList()),
            this.seriesRequestBuilder
                .build(attribute, metricContainerField)
                .collect(Collectors.toUnmodifiableList()),
            this.seriesRequestBuilder
                 .buildBaselineSeriesRequests(attribute, metricContainerField)
                 .collect(Collectors.toUnmodifiableList()),
            (aggList, seriesList, baselineSeriesList) -> new DefaultMetricRequest(attribute, aggList, seriesList, baselineSeriesList))
        .cast(MetricRequest.class)
        .toObservable();
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultMetricRequest implements MetricRequest {
    AttributeModel attribute;
    List<MetricAggregationRequest> aggregationRequests;
    List<MetricSeriesRequest> seriesRequests;
    List<MetricSeriesRequest> baselineSeriesRequests;
  }
}
