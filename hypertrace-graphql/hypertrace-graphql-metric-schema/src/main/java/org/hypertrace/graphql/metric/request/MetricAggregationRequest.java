package org.hypertrace.graphql.metric.request;

import java.util.List;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;

public interface MetricAggregationRequest {

  AttributeModel attribute();

  AttributeModelMetricAggregationType aggregation();

  String alias();

  List<Object> arguments();

  boolean baseline();
}
