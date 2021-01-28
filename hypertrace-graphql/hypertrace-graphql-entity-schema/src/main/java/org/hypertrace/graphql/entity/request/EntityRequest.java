package org.hypertrace.graphql.entity.request;

import java.util.List;
import org.hypertrace.core.graphql.common.request.ResultSetRequest;
import org.hypertrace.graphql.entity.schema.EntityType;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

public interface EntityRequest {
  String entityType();

  ResultSetRequest<AggregatableOrderArgument> resultSetRequest();

  List<MetricRequest> metricRequests();

  EdgeSetGroupRequest incomingEdgeRequests();

  EdgeSetGroupRequest outgoingEdgeRequests();

  boolean includeInactive();

  boolean fetchTotal();
}
