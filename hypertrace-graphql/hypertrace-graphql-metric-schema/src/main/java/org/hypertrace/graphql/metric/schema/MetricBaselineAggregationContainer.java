package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateUnitsArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationPercentileSizeArgument;

public interface MetricBaselineAggregationContainer extends MetricAggregationContainer {
    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_SUM_KEY)
    MetricBaselineAggregation sum();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_MIN_KEY)
    MetricBaselineAggregation min();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_MAX_KEY)
    MetricBaselineAggregation max();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_AVG_KEY)
    MetricBaselineAggregation avg();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_COUNT_KEY)
    MetricBaselineAggregation count();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_DISTINCTCOUNT_KEY)
    MetricBaselineAggregation distinctcount();

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_AVGRATE_KEY)
    MetricBaselineAggregation avgrate(
            @GraphQLNonNull @GraphQLName(MetricAggregationAvgRateSizeArgument.ARGUMENT_NAME) int size,
            @GraphQLNonNull @GraphQLName(MetricAggregationAvgRateUnitsArgument.ARGUMENT_NAME)
                    TimeUnit units);

    @Override
    @GraphQLField
    @GraphQLNonNull
    @GraphQLName(METRIC_AGGREGATION_CONTAINER_PERCENTILE_KEY)
    MetricBaselineAggregation percentile(
            @GraphQLNonNull @GraphQLName(MetricAggregationPercentileSizeArgument.ARGUMENT_NAME) int size);
}

