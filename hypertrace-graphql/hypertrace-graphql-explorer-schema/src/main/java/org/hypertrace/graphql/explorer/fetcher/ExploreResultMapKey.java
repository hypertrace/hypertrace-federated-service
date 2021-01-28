package org.hypertrace.graphql.explorer.fetcher;

import static org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType.AVGRATE;
import static org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType.PERCENTILE;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricArguments;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExploreResultMapKey {
  @Nonnull String key;
  @Nullable MetricAggregationType aggregationType;
  @Nullable List<Object> arguments;

  public static ExploreResultMapKey basicAttribute(String attributeKey) {
    return new ExploreResultMapKey(attributeKey, null, null);
  }

  public static ExploreResultMapKey forAggregationRequest(
      MetricAggregationRequest aggregationRequest, MetricAggregationType aggregationType) {
    return new ExploreResultMapKey(
        aggregationRequest.attribute().key(), aggregationType, aggregationRequest.arguments());
  }

  public static ExploreResultMapKey basicAggregation(
      String attributeKey, MetricAggregationType aggregationType) {
    return new ExploreResultMapKey(attributeKey, aggregationType, Collections.emptyList());
  }

  public static ExploreResultMapKey avgRateAggregation(String attributeKey, Duration duration) {
    return new ExploreResultMapKey(
        attributeKey, AVGRATE, MetricArguments.avgRateWithPeriod(duration));
  }

  public static ExploreResultMapKey percentileAggregation(String attributeKey, int value) {
    return new ExploreResultMapKey(
        attributeKey, PERCENTILE, MetricArguments.percentileWithSize(value));
  }

  public static ExploreResultMapKey intervalStart() {
    return new ExploreResultMapKey("__intervalStart__", null, null);
  }
}
