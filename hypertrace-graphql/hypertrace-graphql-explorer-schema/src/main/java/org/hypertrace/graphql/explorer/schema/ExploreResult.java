package org.hypertrace.graphql.explorer.schema;

import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import java.time.Instant;
import java.util.Map;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey;
import org.hypertrace.graphql.explorer.fetcher.ExplorerSelectionFetcher;
import org.hypertrace.graphql.explorer.schema.argument.SelectionAggregationTypeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionKeyArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionSizeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionUnitArgument;

public interface ExploreResult {

  String EXPLORE_RESULT_SELECTION_KEY = "selection";
  String EXPLORE_RESULT_INTERVAL_START_KEY = "intervalStart";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(EXPLORE_RESULT_SELECTION_KEY)
  @GraphQLDataFetcher(ExplorerSelectionFetcher.class)
  default Selection selection(
      @GraphQLName(SelectionKeyArgument.ARGUMENT_NAME) @GraphQLNonNull String key,
      @GraphQLName(SelectionAggregationTypeArgument.ARGUMENT_NAME)
          MetricAggregationType aggregationType,
      @GraphQLName(SelectionSizeArgument.ARGUMENT_NAME) int size,
      @GraphQLName(SelectionUnitArgument.ARGUMENT_NAME) TimeUnit units) {
    return null;
  }

  @GraphQLField
  @GraphQLName(EXPLORE_RESULT_INTERVAL_START_KEY)
  Instant intervalStart();

  // Needed in schema for selection fetcher, unfortunately
  Map<ExploreResultMapKey, Selection> selectionMap();
}
