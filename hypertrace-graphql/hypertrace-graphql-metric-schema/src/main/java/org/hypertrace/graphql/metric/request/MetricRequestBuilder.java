package org.hypertrace.graphql.metric.request;

import graphql.schema.SelectedField;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.stream.Stream;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

public interface MetricRequestBuilder {

  Single<List<MetricRequest>> build(
      GraphQlRequestContext context,
      String requestScope,
      Stream<SelectedField> metricQueryableFieldStream);
}
