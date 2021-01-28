package org.hypertrace.graphql.metric.request;

import java.time.Duration;

public interface MetricSeriesRequest {
  Duration period();

  MetricAggregationRequest aggregationRequest();

  String alias();
}
