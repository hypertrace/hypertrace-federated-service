package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.metric.schema.argument.MetricIntervalSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricIntervalUnitsArgument;

public interface MetricIntervalContainer {

  String METRIC_INTERVAL_CONTAINER_SERIES_KEY = "series";
  String BASELINE_INTERVAL_CONTAINER_SERIES_KEY = "baselineSeries";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_INTERVAL_CONTAINER_SERIES_KEY)
  List<MetricInterval> series(
      @GraphQLName(MetricIntervalSizeArgument.ARGUMENT_NAME) @GraphQLNonNull int size,
      @GraphQLName(MetricIntervalUnitsArgument.ARGUMENT_NAME) @GraphQLNonNull TimeUnit units);

  @GraphQLField
  @GraphQLName(BASELINE_INTERVAL_CONTAINER_SERIES_KEY)
  List<BaselinedMetricInterval> baselineSeries(
      @GraphQLName(MetricIntervalSizeArgument.ARGUMENT_NAME) @GraphQLNonNull int size,
      @GraphQLName(MetricIntervalUnitsArgument.ARGUMENT_NAME) @GraphQLNonNull TimeUnit units);
}
