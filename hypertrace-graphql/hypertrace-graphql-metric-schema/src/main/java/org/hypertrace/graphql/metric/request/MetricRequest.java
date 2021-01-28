package org.hypertrace.graphql.metric.request;

import java.util.List;
import org.hypertrace.core.graphql.attributes.AttributeModel;

public interface MetricRequest {
  AttributeModel attribute();

  List<MetricAggregationRequest> aggregationRequests();

  List<MetricSeriesRequest> seriesRequests();

  List<MetricSeriesRequest> baselineSeriesRequests();
}
