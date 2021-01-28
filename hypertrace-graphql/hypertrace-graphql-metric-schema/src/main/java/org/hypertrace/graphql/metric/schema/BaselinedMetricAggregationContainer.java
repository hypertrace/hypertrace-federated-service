package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateUnitsArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationPercentileSizeArgument;

public interface BaselinedMetricAggregationContainer extends MetricAggregationContainer {

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_SUM_KEY)
    BaselinedMetricAggregation sum();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_MIN_KEY)
    BaselinedMetricAggregation min();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_MAX_KEY)
    BaselinedMetricAggregation max();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_AVG_KEY)
    BaselinedMetricAggregation avg();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_COUNT_KEY)
    BaselinedMetricAggregation count();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_DISTINCTCOUNT_KEY)
    BaselinedMetricAggregation distinctcount();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_AVGRATE_KEY)
    BaselinedMetricAggregation avgrate(
            @GraphQLNonNull @GraphQLName(MetricAggregationAvgRateSizeArgument.ARGUMENT_NAME) int size,
            @GraphQLNonNull @GraphQLName(MetricAggregationAvgRateUnitsArgument.ARGUMENT_NAME)
                    TimeUnit units);

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_PERCENTILE_KEY)
    BaselinedMetricAggregation percentile(
            @GraphQLNonNull @GraphQLName(MetricAggregationPercentileSizeArgument.ARGUMENT_NAME) int size);
}
