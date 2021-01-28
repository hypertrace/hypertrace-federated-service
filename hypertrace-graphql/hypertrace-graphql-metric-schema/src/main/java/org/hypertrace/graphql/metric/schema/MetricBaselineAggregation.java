package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

public interface MetricBaselineAggregation extends MetricAggregation {

    String BASELINE_AGGREGATION_LOWER_VALUE = "lowerBound";
    String BASELINE_AGGREGATION_UPPER_VALUE = "upperBound";

    @GraphQLName(BASELINE_AGGREGATION_LOWER_VALUE)
    @GraphQLField
    Double lowerBound();

    @GraphQLName(BASELINE_AGGREGATION_UPPER_VALUE)
    @GraphQLField
    Double upperBound();

}
