package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;

import java.time.Instant;

@GraphQLName(BaselinedMetricInterval.BASELINE_METRIC_INTERVAL)
public interface BaselinedMetricInterval extends MetricBaselineAggregationContainer {
    String BASELINE_METRIC_INTERVAL = "baselineInterval";

    @GraphQLField
    @GraphQLNonNull
    Instant startTime();

    @GraphQLField
    @GraphQLNonNull
    Instant endTime();

}
