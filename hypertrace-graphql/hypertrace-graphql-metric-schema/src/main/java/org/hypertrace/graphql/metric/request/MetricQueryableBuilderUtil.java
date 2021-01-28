package org.hypertrace.graphql.metric.request;

import static org.hypertrace.graphql.metric.schema.MetricAggregationQueryable.METRIC_FIELD_NAME;

import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.core.graphql.utils.schema.SelectionQuery;
import org.hypertrace.graphql.metric.schema.argument.MetricKeyArgument;

class MetricQueryableBuilderUtil {

  private final GraphQlSelectionFinder selectionFinder;
  private final ArgumentDeserializer argumentDeserializer;
  private final AttributeStore attributeStore;

  @Inject
  MetricQueryableBuilderUtil(
      GraphQlSelectionFinder selectionFinder,
      ArgumentDeserializer argumentDeserializer,
      AttributeStore attributeStore) {
    this.selectionFinder = selectionFinder;
    this.argumentDeserializer = argumentDeserializer;
    this.attributeStore = attributeStore;
  }

  <T> Single<List<T>> buildForEachMetricQueryable(
      GraphQlRequestContext context,
      String scope,
      Stream<SelectedField> metricQueryableFieldStream,
      BiFunction<AttributeModel, SelectedField, Observable<T>> builder) {

    return Observable.fromStream(metricQueryableFieldStream)
        .flatMap(field -> this.buildForEachMetricContainer(context, scope, field, builder))
        .collect(Collectors.toList());
  }

  private <T> Observable<T> buildForEachMetricContainer(
      GraphQlRequestContext context,
      String scope,
      SelectedField metricQueryableField,
      BiFunction<AttributeModel, SelectedField, Observable<T>> builder) {

    return Observable.fromStream(
            this.selectionFinder.findSelections(
                metricQueryableField.getSelectionSet(),
                SelectionQuery.namedChild(METRIC_FIELD_NAME)))
        .flatMap(
            field ->
                this.buildForEachMetricContainer(
                    context, scope, field, attribute -> builder.apply(attribute, field)));
  }

  private <T> Observable<T> buildForEachMetricContainer(
      GraphQlRequestContext context,
      String scope,
      SelectedField metricContainerField,
      Function<AttributeModel, Observable<T>> builder) {
    Optional<String> metricKey =
        this.argumentDeserializer.deserializePrimitive(
            metricContainerField.getArguments(), MetricKeyArgument.class);
    return Maybe.fromOptional(metricKey)
        .flatMapSingle(key -> this.attributeStore.get(context, scope, key))
        .flatMapObservable(builder::apply);
  }
}
