package org.hypertrace.graphql.metric.request;

import static org.hypertrace.graphql.metric.schema.MetricIntervalContainer.BASELINE_INTERVAL_CONTAINER_SERIES_KEY;
import static org.hypertrace.graphql.metric.schema.MetricIntervalContainer.METRIC_INTERVAL_CONTAINER_SERIES_KEY;

import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Observable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;
import org.hypertrace.graphql.metric.schema.argument.MetricIntervalSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricIntervalUnitsArgument;

class MetricSeriesRequestBuilder {

  private final GraphQlSelectionFinder selectionFinder;
  private final ArgumentDeserializer argumentDeserializer;
  private final MetricAggregationRequestBuilder aggregationRequestBuilder;

  @Inject
  MetricSeriesRequestBuilder(
      GraphQlSelectionFinder selectionFinder,
      ArgumentDeserializer argumentDeserializer,
      MetricAggregationRequestBuilder aggregationRequestBuilder) {
    this.selectionFinder = selectionFinder;
    this.argumentDeserializer = argumentDeserializer;
    this.aggregationRequestBuilder = aggregationRequestBuilder;
  }

  Observable<MetricSeriesRequest> build(
      AttributeModel attribute, SelectedField metricContainerField) {
    return Observable.fromStream(
            this.selectionFinder.findSelections(
                metricContainerField.getSelectionSet(),
                SelectionQuery.namedChild(METRIC_INTERVAL_CONTAINER_SERIES_KEY)))
        .flatMap(field -> this.flattenAggregationsForSeries(attribute, field));
  }

  Observable<MetricSeriesRequest> buildBaselineSeriesRequests(
          AttributeModel attribute, SelectedField metricContainerField) {
    return Observable.fromStream(
            this.selectionFinder.findSelections(
                    metricContainerField.getSelectionSet(),
                    SelectionQuery.namedChild(BASELINE_INTERVAL_CONTAINER_SERIES_KEY)))
            .flatMap(field -> this.flattenAggregationsForSeries(attribute, field));
  }


  private Observable<MetricSeriesRequest> flattenAggregationsForSeries(
      AttributeModel attribute, SelectedField seriesField) {
    Duration period = this.buildSeriesPeriod(seriesField);
    return this.aggregationRequestBuilder
        .build(attribute, seriesField)
        .map(aggregation -> new DefaultMetricSeriesRequest(aggregation, period));
  }

  private Duration buildSeriesPeriod(SelectedField seriesField) {
    Integer size =
        this.argumentDeserializer
            .deserializePrimitive(seriesField.getArguments(), MetricIntervalSizeArgument.class)
            .orElseThrow();

    ChronoUnit units =
        this.argumentDeserializer
            .deserializePrimitive(seriesField.getArguments(), MetricIntervalUnitsArgument.class)
            .map(TimeUnit::getChronoUnit)
            .orElseThrow();
    return Duration.of(size, units);
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultMetricSeriesRequest implements MetricSeriesRequest {
    MetricAggregationRequest aggregationRequest;
    Duration period;

    @Override
    public String alias() {
      return String.format("%s_%s", this.aggregationRequest.alias(), this.period);
    }
  }
}
