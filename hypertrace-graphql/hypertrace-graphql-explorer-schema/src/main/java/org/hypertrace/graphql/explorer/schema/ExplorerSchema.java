package org.hypertrace.graphql.explorer.schema;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLDeprecate;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.util.List;
import org.hypertrace.core.graphql.common.schema.arguments.TimeRangeArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.page.LimitArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.page.OffsetArgument;
import org.hypertrace.core.graphql.common.schema.results.arguments.space.SpaceArgument;
import org.hypertrace.graphql.explorer.fetcher.ExplorerFetcher;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContext;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContextArgument;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerScopeArgument;
import org.hypertrace.graphql.explorer.schema.argument.GroupByArgument;
import org.hypertrace.graphql.explorer.schema.argument.IntervalArgument;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

public interface ExplorerSchema {

  String EXPLORE_QUERY_NAME = "explore";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(EXPLORE_QUERY_NAME)
  @GraphQLDataFetcher(ExplorerFetcher.class)
  ExploreResultSet explore(
      @Deprecated @GraphQLName(ExplorerContextArgument.ARGUMENT_NAME) ExplorerContext context,
      @GraphQLName(ExplorerScopeArgument.ARGUMENT_NAME) String scope,
      @GraphQLName(TimeRangeArgument.ARGUMENT_NAME) @GraphQLNonNull TimeRangeArgument timeRange,
      @GraphQLName(SpaceArgument.ARGUMENT_NAME) String space,
      @GraphQLName(LimitArgument.ARGUMENT_NAME) int limit,
      @GraphQLName(OffsetArgument.ARGUMENT_NAME) int offset,
      @GraphQLName(FilterArgument.ARGUMENT_NAME) List<FilterArgument> filterBy,
      @GraphQLName(OrderArgument.ARGUMENT_NAME) List<AggregatableOrderArgument> orderBy,
      @GraphQLName(GroupByArgument.ARGUMENT_NAME) GroupByArgument groupBy,
      @GraphQLName(IntervalArgument.ARGUMENT_NAME) IntervalArgument interval);
}
