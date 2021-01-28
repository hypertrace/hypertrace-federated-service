package org.hypertrace.graphql.metric.schema;

import graphql.annotations.annotationTypes.GraphQLName;

@GraphQLName(MetricContainer.METRIC_CONTAINER_TYPE_NAME)
public interface MetricContainer extends BaselinedMetricAggregationContainer, MetricIntervalContainer {
  String METRIC_CONTAINER_TYPE_NAME = "MetricContainer";
}
