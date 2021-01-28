package org.hypertrace.graphql.metric.request;

import java.time.Duration;
import java.util.List;

public class MetricArguments {

  public static List<Object> avgRateWithPeriod(Duration period) {
    return List.of(period.toSeconds());
  }

  public static List<Object> percentileWithSize(int size) {
    return List.of(size);
  }
}
