package org.hypertrace.graphql.metric.schema.argument;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import javax.annotation.Nullable;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;

@GraphQLName(AggregatableOrderArgument.TYPE_NAME)
public interface AggregatableOrderArgument extends OrderArgument {
  String ARGUMENT_NAME = "orderBy"; // TODO rename to order
  String TYPE_NAME = "AggregatableOrder";
  String METRIC_AGGREGATION_ORDER_AGGREGATION_TYPE = "aggregation";
  String METRIC_AGGREGATION_ORDER_AGGREGATION_SIZE = "size";

  @GraphQLField
  @GraphQLName(METRIC_AGGREGATION_ORDER_AGGREGATION_TYPE)
  @Nullable
  MetricAggregationType aggregation();

  // For percentile this is needed.
  @GraphQLField
  @GraphQLName(METRIC_AGGREGATION_ORDER_AGGREGATION_SIZE)
  @Nullable
  Integer size();
}
