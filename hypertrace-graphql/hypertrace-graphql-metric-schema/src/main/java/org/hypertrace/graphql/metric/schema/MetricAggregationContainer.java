package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateUnitsArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationPercentileSizeArgument;

public interface MetricAggregationContainer {
  String METRIC_AGGREGATION_CONTAINER_SUM_KEY = "sum";
  String METRIC_AGGREGATION_CONTAINER_MIN_KEY = "min";
  String METRIC_AGGREGATION_CONTAINER_MAX_KEY = "max";
  String METRIC_AGGREGATION_CONTAINER_AVG_KEY = "avg";
  String METRIC_AGGREGATION_CONTAINER_AVGRATE_KEY = "avgrate";
  String METRIC_AGGREGATION_CONTAINER_PERCENTILE_KEY = "percentile";
  String METRIC_AGGREGATION_CONTAINER_COUNT_KEY = "count";
  String METRIC_AGGREGATION_CONTAINER_DISTINCTCOUNT_KEY = "distinctcount";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_SUM_KEY)
  MetricAggregation sum();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_MIN_KEY)
  MetricAggregation min();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_MAX_KEY)
  MetricAggregation max();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_AVG_KEY)
  MetricAggregation avg();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_COUNT_KEY)
  MetricAggregation count();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_DISTINCTCOUNT_KEY)
  MetricAggregation distinctcount();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_AVGRATE_KEY)
  MetricAggregation avgrate(
      @GraphQLNonNull @GraphQLName(MetricAggregationAvgRateSizeArgument.ARGUMENT_NAME) int size,
      @GraphQLNonNull @GraphQLName(MetricAggregationAvgRateUnitsArgument.ARGUMENT_NAME)
          TimeUnit units);

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(METRIC_AGGREGATION_CONTAINER_PERCENTILE_KEY)
  MetricAggregation percentile(
      @GraphQLNonNull @GraphQLName(MetricAggregationPercentileSizeArgument.ARGUMENT_NAME) int size);
}
