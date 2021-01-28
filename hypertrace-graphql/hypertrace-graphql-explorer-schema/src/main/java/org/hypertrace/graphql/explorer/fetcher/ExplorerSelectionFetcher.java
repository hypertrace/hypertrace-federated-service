package org.hypertrace.graphql.explorer.fetcher;

import static java.util.Objects.requireNonNull;
import static org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey.avgRateAggregation;
import static org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey.basicAggregation;
import static org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey.basicAttribute;
import static org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey.percentileAggregation;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.graphql.explorer.schema.ExploreResult;
import org.hypertrace.graphql.explorer.schema.Selection;
import org.hypertrace.graphql.explorer.schema.argument.SelectionAggregationTypeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionKeyArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionSizeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionUnitArgument;

/**
 * This class is needed because gql annotations doesn't understand nullability for default generated
 * fetchers. That is, if only one arg is provided - it tries to invoke via reflection a one arg
 * version of the method that doesn't exist. It also doesn't understand overloads.
 */
public class ExplorerSelectionFetcher extends InjectableDataFetcher<Selection> {

  public ExplorerSelectionFetcher() {
    super(ExplorerSelectionFetcherImpl.class);
  }

  private static class ExplorerSelectionFetcherImpl
      implements DataFetcher<CompletableFuture<Selection>> {

    private final ArgumentDeserializer argumentDeserializer;

    @Inject
    ExplorerSelectionFetcherImpl(ArgumentDeserializer argumentDeserializer) {
      this.argumentDeserializer = argumentDeserializer;
    }

    @Override
    public CompletableFuture<Selection> get(DataFetchingEnvironment environment) {

      return CompletableFuture.completedFuture(
          environment
              .<ExploreResult>getSource()
              .selectionMap()
              .get(this.getKeyForArguments(environment.getArguments())));
    }

    private ExploreResultMapKey getKeyForArguments(Map<String, Object> arguments) {
      String key =
          this.argumentDeserializer
              .deserializePrimitive(arguments, SelectionKeyArgument.class)
              .orElseThrow();
      @Nullable
      MetricAggregationType aggregationType =
          this.argumentDeserializer
              .deserializePrimitive(arguments, SelectionAggregationTypeArgument.class)
              .orElse(null);
      if (aggregationType == null) {
        return basicAttribute(key);
      }

      @Nullable
      Integer selectionSize =
          this.argumentDeserializer
              .deserializePrimitive(arguments, SelectionSizeArgument.class)
              .orElse(null);

      @Nullable
      ChronoUnit selectionUnit =
          this.argumentDeserializer
              .deserializePrimitive(arguments, SelectionUnitArgument.class)
              .map(TimeUnit::getChronoUnit)
              .orElse(null);

      switch (aggregationType) {
        case AVGRATE:
          return avgRateAggregation(
              key, Duration.of(requireNonNull(selectionSize), requireNonNull(selectionUnit)));
        case PERCENTILE:
          return percentileAggregation(key, requireNonNull(selectionSize));
        case COUNT:
        case AVG:
        case SUM:
        case MIN:
        case MAX:
        case DISTINCTCOUNT:
        default:
          return basicAggregation(key, aggregationType);
      }
    }
  }
}
