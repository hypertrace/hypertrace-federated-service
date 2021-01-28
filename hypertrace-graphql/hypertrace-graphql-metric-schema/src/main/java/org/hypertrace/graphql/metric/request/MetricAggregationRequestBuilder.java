package org.hypertrace.graphql.metric.request;

import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.stream.Stream;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

public interface MetricAggregationRequestBuilder {
  Observable<MetricAggregationRequest> build(
      AttributeModel attribute, SelectedField metricAggregationContainerField);

  Single<List<MetricAggregationRequest>> build(
      GraphQlRequestContext context,
      String requestScope,
      Stream<SelectedField> metricQueryableFieldStream);

  MetricAggregationRequest build(
      AttributeModel attribute,
      AttributeModelMetricAggregationType aggregationType,
      List<Object> arguments);

  MetricAggregationRequest build(
          AttributeModel attribute,
          AttributeModelMetricAggregationType aggregationType,
          List<Object> arguments,
          boolean baseline);
}
