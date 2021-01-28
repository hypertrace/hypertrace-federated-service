package org.hypertrace.graphql.metric.deserialization;

import static org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig.forPrimitive;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationAvgRateUnitsArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricAggregationPercentileSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricIntervalSizeArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricIntervalUnitsArgument;
import org.hypertrace.graphql.metric.schema.argument.MetricKeyArgument;

public class MetricDeserializationModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ArgumentDeserializationConfig> deserializationConfigMultibinder =
        Multibinder.newSetBinder(binder(), ArgumentDeserializationConfig.class);
    deserializationConfigMultibinder
        .addBinding()
        .toInstance(forPrimitive(MetricKeyArgument.ARGUMENT_NAME, MetricKeyArgument.class));

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            forPrimitive(
                MetricIntervalSizeArgument.ARGUMENT_NAME, MetricIntervalSizeArgument.class));
    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            forPrimitive(
                MetricIntervalUnitsArgument.ARGUMENT_NAME, MetricIntervalUnitsArgument.class));
    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            forPrimitive(
                MetricAggregationAvgRateSizeArgument.ARGUMENT_NAME,
                MetricAggregationAvgRateSizeArgument.class));
    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            forPrimitive(
                MetricAggregationAvgRateUnitsArgument.ARGUMENT_NAME,
                MetricAggregationAvgRateUnitsArgument.class));
    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            forPrimitive(
                MetricAggregationPercentileSizeArgument.ARGUMENT_NAME,
                MetricAggregationPercentileSizeArgument.class));
    deserializationConfigMultibinder
        .addBinding()
        .to(AggregatableOrderArgumentDeserializationConfig.class);
  }
}
