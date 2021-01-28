package org.hypertrace.graphql.metric.request;

import static org.hypertrace.graphql.metric.schema.BaselinedMetricAggregation.BASELINE_AGGREGATION_VALUE;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_AVGRATE_KEY;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_AVG_KEY;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_COUNT_KEY;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_DISTINCTCOUNT_KEY;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_MAX_KEY;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_MIN_KEY;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_PERCENTILE_KEY;
import static org.hypertrace.graphql.metric.schema.MetricAggregationContainer.METRIC_AGGREGATION_CONTAINER_SUM_KEY;

import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateUnitsArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationPercentileSizeArgument;

class DefaultMetricAggregationRequestBuilder implements MetricAggregationRequestBuilder {

  private final GraphQlSelectionFinder selectionFinder;
  private final ArgumentDeserializer argumentDeserializer;
  private final MetricQueryableBuilderUtil metricQueryableBuilderUtil;

  @Inject
  DefaultMetricAggregationRequestBuilder(
      GraphQlSelectionFinder selectionFinder,
      ArgumentDeserializer argumentDeserializer,
      MetricQueryableBuilderUtil metricQueryableBuilderUtil) {
    this.selectionFinder = selectionFinder;
    this.argumentDeserializer = argumentDeserializer;
    this.metricQueryableBuilderUtil = metricQueryableBuilderUtil;
  }

  @Override
  public Observable<MetricAggregationRequest> build(
      AttributeModel attribute, SelectedField metricAggregationContainerField) {
    return Observable.fromStream(
            this.selectionFinder.findSelections(
                metricAggregationContainerField.getSelectionSet(),
                SelectionQuery.builder()
                    .matchesPredicate(field -> this.getAggregationTypeForField(field).isPresent())
                    .build()))
        .map(selectedField -> this.requestForAggregationField(attribute, selectedField));
  }

  @Override
  public Single<List<MetricAggregationRequest>> build(
      GraphQlRequestContext context,
      String requestScope,
      Stream<SelectedField> metricQueryableFieldStream) {
    return this.metricQueryableBuilderUtil.buildForEachMetricQueryable(
        context, requestScope, metricQueryableFieldStream, this::build);
  }

  public MetricAggregationRequest build(
      AttributeModel attribute,
      AttributeModelMetricAggregationType aggregationType,
      List<Object> arguments) {
    return this.build(attribute, aggregationType, arguments, false);
  }

  public MetricAggregationRequest build(
      AttributeModel attribute,
      AttributeModelMetricAggregationType aggregationType,
      List<Object> arguments,
      boolean baseline) {
    return new DefaultMetricAggregationRequest(attribute, aggregationType, arguments, baseline);
  }

  private MetricAggregationRequest requestForAggregationField(
      AttributeModel attribute, SelectedField field) {
    AttributeModelMetricAggregationType aggregationType =
        this.getAggregationTypeForField(field).orElseThrow();
    return this.build(
        attribute,
        aggregationType,
        this.getArgumentsForAggregation(aggregationType, field.getArguments()),
            this.selectionFinder.findSelections(field.getSelectionSet(),
                    SelectionQuery.namedChild(BASELINE_AGGREGATION_VALUE)).findAny().isPresent());
  }

  private Optional<AttributeModelMetricAggregationType> getAggregationTypeForField(
      SelectedField field) {
    switch (field.getName()) {
      case METRIC_AGGREGATION_CONTAINER_SUM_KEY:
        return Optional.of(AttributeModelMetricAggregationType.SUM);
      case METRIC_AGGREGATION_CONTAINER_AVG_KEY:
        return Optional.of(AttributeModelMetricAggregationType.AVG);
      case METRIC_AGGREGATION_CONTAINER_MIN_KEY:
        return Optional.of(AttributeModelMetricAggregationType.MIN);
      case METRIC_AGGREGATION_CONTAINER_MAX_KEY:
        return Optional.of(AttributeModelMetricAggregationType.MAX);
      case METRIC_AGGREGATION_CONTAINER_AVGRATE_KEY:
        return Optional.of(AttributeModelMetricAggregationType.AVGRATE);
      case METRIC_AGGREGATION_CONTAINER_PERCENTILE_KEY:
        return Optional.of(AttributeModelMetricAggregationType.PERCENTILE);
      case METRIC_AGGREGATION_CONTAINER_COUNT_KEY:
        return Optional.of(AttributeModelMetricAggregationType.COUNT);
      case METRIC_AGGREGATION_CONTAINER_DISTINCTCOUNT_KEY:
        return Optional.of(AttributeModelMetricAggregationType.DISTINCT_COUNT);
      default:
        return Optional.empty();
    }
  }

  private List<Object> getArgumentsForAggregation(
      AttributeModelMetricAggregationType aggregation, Map<String, Object> arguments) {
    switch (aggregation) {
      case AVGRATE:
        return MetricArguments.avgRateWithPeriod(this.avgRateDuration(arguments));
      case PERCENTILE:
        return MetricArguments.percentileWithSize(this.percentileSize(arguments));
      case COUNT:
      case AVG:
      case SUM:
      case MIN:
      case MAX:
      case DISTINCT_COUNT:
      default:
        return Collections.emptyList();
    }
  }

  private Duration avgRateDuration(Map<String, Object> arguments) {
    Integer size =
        this.argumentDeserializer
            .deserializePrimitive(arguments, MetricAggregationAvgRateSizeArgument.class)
            .orElseThrow();

    ChronoUnit units =
        this.argumentDeserializer
            .deserializePrimitive(arguments, MetricAggregationAvgRateUnitsArgument.class)
            .map(TimeUnit::getChronoUnit)
            .orElseThrow();

    return Duration.of(size, units);
  }

  private Integer percentileSize(Map<String, Object> arguments) {
    return this.argumentDeserializer
        .deserializePrimitive(arguments, MetricAggregationPercentileSizeArgument.class)
        .orElseThrow();
  }

  @Value
  @Accessors(fluent = true)
  private static class DefaultMetricAggregationRequest implements MetricAggregationRequest {
    AttributeModel attribute;
    AttributeModelMetricAggregationType aggregation;
    List<Object> arguments;
    boolean baseline;

    @Override
    public String alias() {
      return String.format(
          "%s_%s_%s", this.aggregation.name(), this.attribute.id(), this.arguments);
    }

    @Override
    public boolean baseline() {
      return baseline;
    }
  }
}
