package org.hypertrace.graphql.explorer.request;

import static org.hypertrace.core.graphql.common.schema.results.ResultSet.RESULT_SET_RESULTS_NAME;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.AttributeRequestBuilder;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;
import org.hypertrace.graphql.explorer.schema.ExploreResult;
import org.hypertrace.graphql.explorer.schema.argument.SelectionAggregationTypeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionKeyArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionSizeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionUnitArgument;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricAggregationRequestBuilder;
import org.hypertrace.graphql.metric.request.MetricArguments;

class ExploreSelectionRequestBuilder {

  private final GraphQlSelectionFinder selectionFinder;
  private final ArgumentDeserializer argumentDeserializer;
  private final AttributeStore attributeStore;
  private final Converter<MetricAggregationType, AttributeModelMetricAggregationType>
      aggregationTypeConverter;
  private final AttributeRequestBuilder attributeRequestBuilder;
  private final MetricAggregationRequestBuilder aggregationRequestBuilder;

  @Inject
  ExploreSelectionRequestBuilder(
      GraphQlSelectionFinder selectionFinder,
      ArgumentDeserializer argumentDeserializer,
      AttributeStore attributeStore,
      Converter<MetricAggregationType, AttributeModelMetricAggregationType>
          aggregationTypeConverter,
      AttributeRequestBuilder attributeRequestBuilder,
      MetricAggregationRequestBuilder aggregationRequestBuilder) {
    this.selectionFinder = selectionFinder;
    this.argumentDeserializer = argumentDeserializer;
    this.attributeStore = attributeStore;
    this.aggregationTypeConverter = aggregationTypeConverter;
    this.attributeRequestBuilder = attributeRequestBuilder;
    this.aggregationRequestBuilder = aggregationRequestBuilder;
  }

  Single<Set<AttributeRequest>> getAttributeSelections(
      GraphQlRequestContext requestContext,
      String explorerScope,
      DataFetchingFieldSelectionSet exploreSelectionSet) {
    return this.getSelections(
            requestContext, explorerScope, exploreSelectionSet, SelectionType.ATTRIBUTE)
        .map(arguments -> this.attributeRequestBuilder.buildForAttribute(arguments.getAttribute()))
        .collect(Collectors.toUnmodifiableSet());
  }

  Single<Set<MetricAggregationRequest>> getAggregationSelections(
      GraphQlRequestContext requestContext,
      String explorerScope,
      DataFetchingFieldSelectionSet exploreSelectionSet) {
    return this.buildAggregationRequests(requestContext, explorerScope, exploreSelectionSet)
        .collect(Collectors.toUnmodifiableSet());
  }

  private Observable<SelectionArguments> getSelections(
      GraphQlRequestContext requestContext,
      String explorerScope,
      DataFetchingFieldSelectionSet exploreSelectionSet,
      SelectionType selectionType) {
    return Observable.fromStream(
            this.selectionFinder.findSelections(
                exploreSelectionSet,
                SelectionQuery.builder()
                    .selectionPath(
                        List.of(
                            RESULT_SET_RESULTS_NAME, ExploreResult.EXPLORE_RESULT_SELECTION_KEY))
                    .build()))
        .flatMapSingle(field -> this.getSelectionArguments(requestContext, explorerScope, field))
        .filter(arguments -> arguments.getSelectionType().equals(selectionType));
  }

  private Single<SelectionArguments> getSelectionArguments(
      GraphQlRequestContext requestContext, String explorerScope, SelectedField selectedField) {
    String key =
        this.argumentDeserializer
            .deserializePrimitive(selectedField.getArguments(), SelectionKeyArgument.class)
            .orElseThrow();

    return this.attributeStore
        .get(requestContext, explorerScope, key)
        .map(attribute -> this.getSelectionArguments(attribute, selectedField));
  }

  private SelectionArguments getSelectionArguments(
      AttributeModel attribute, SelectedField selectedField) {
    return this.getAggregationType(selectedField)
        .map(
            aggregationType ->
                new SelectionArguments(
                    attribute, aggregationType, this.getArguments(selectedField, aggregationType)))
        .orElse(new SelectionArguments(attribute));
  }

  private Optional<AttributeModelMetricAggregationType> getAggregationType(
      SelectedField selectedField) {
    return this.argumentDeserializer
        .deserializePrimitive(selectedField.getArguments(), SelectionAggregationTypeArgument.class)
        .map(this.aggregationTypeConverter::convert)
        .map(Single::blockingGet)
        .or(Optional::empty);
  }

  Observable<MetricAggregationRequest> buildAggregationRequests(
      GraphQlRequestContext requestContext,
      String explorerScope,
      DataFetchingFieldSelectionSet exploreSelectionSet) {
    return this.getSelections(
            requestContext, explorerScope, exploreSelectionSet, SelectionType.AGGREGATION)
        .map(this::buildAggregationRequest);
  }

  private MetricAggregationRequest buildAggregationRequest(SelectionArguments arguments) {
    return this.aggregationRequestBuilder.build(
        arguments.getAttribute(),
        arguments.getAggregationType(),
        arguments.getAggregationArguments());
  }

  private List<Object> getArguments(
      SelectedField selectedField, AttributeModelMetricAggregationType aggregationType) {
    Optional<ChronoUnit> unit =
        this.argumentDeserializer
            .deserializePrimitive(selectedField.getArguments(), SelectionUnitArgument.class)
            .map(TimeUnit::getChronoUnit);

    Optional<Integer> size =
        this.argumentDeserializer.deserializePrimitive(
            selectedField.getArguments(), SelectionSizeArgument.class);
    switch (aggregationType) {
      case AVGRATE:
        return MetricArguments.avgRateWithPeriod(
            Duration.of(size.orElseThrow(), unit.orElseThrow()));
      case PERCENTILE:
        return MetricArguments.percentileWithSize(size.orElseThrow());
      case DISTINCT_COUNT:
      case COUNT:
      case AVG:
      case SUM:
      case MIN:
      case MAX:
      default:
        return Collections.emptyList();
    }
  }

  private enum SelectionType {
    ATTRIBUTE,
    AGGREGATION
  }

  @Value
  @AllArgsConstructor
  private static class SelectionArguments {
    @Nonnull SelectionType selectionType;
    @Nonnull AttributeModel attribute;
    @Nullable AttributeModelMetricAggregationType aggregationType;
    @Nullable List<Object> aggregationArguments;

    SelectionArguments(AttributeModel attribute) {
      this(SelectionType.ATTRIBUTE, attribute, null, null);
    }

    SelectionArguments(
        AttributeModel attribute,
        AttributeModelMetricAggregationType aggregationType,
        List<Object> aggregationArguments) {
      this(SelectionType.AGGREGATION, attribute, aggregationType, aggregationArguments);
    }
  }
}
