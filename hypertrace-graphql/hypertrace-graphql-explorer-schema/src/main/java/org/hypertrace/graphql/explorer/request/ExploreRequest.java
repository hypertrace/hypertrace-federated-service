package org.hypertrace.graphql.explorer.request;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.graphql.explorer.schema.argument.IntervalArgument;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

public interface ExploreRequest {
  GraphQlRequestContext requestContext();

  String scope();

  TimeRangeArgument timeRange();

  int limit();

  int offset();

  Set<AttributeRequest> attributeRequests();

  Set<MetricAggregationRequest> aggregationRequests();

  Optional<IntervalArgument> timeInterval();

  List<AttributeAssociation<AggregatableOrderArgument>> orderArguments();

  List<AttributeAssociation<FilterArgument>> filterArguments();

  Set<AttributeRequest> groupByAttributeRequests();

  boolean includeRest();

  Optional<String> spaceId();
}
