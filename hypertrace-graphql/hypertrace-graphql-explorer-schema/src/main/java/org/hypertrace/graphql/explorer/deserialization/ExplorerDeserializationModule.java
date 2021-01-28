package org.hypertrace.graphql.explorer.deserialization;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContextArgument;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerScopeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionAggregationTypeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionKeyArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionSizeArgument;
import org.hypertrace.graphql.explorer.schema.argument.SelectionUnitArgument;

public class ExplorerDeserializationModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ArgumentDeserializationConfig> deserializationConfigMultibinder =
        Multibinder.newSetBinder(binder(), ArgumentDeserializationConfig.class);

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                ExplorerContextArgument.ARGUMENT_NAME, ExplorerContextArgument.class));

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                ExplorerScopeArgument.ARGUMENT_NAME, ExplorerScopeArgument.class));

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                SelectionAggregationTypeArgument.ARGUMENT_NAME,
                SelectionAggregationTypeArgument.class));

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                SelectionKeyArgument.ARGUMENT_NAME, SelectionKeyArgument.class));

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                SelectionSizeArgument.ARGUMENT_NAME, SelectionSizeArgument.class));

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                SelectionUnitArgument.ARGUMENT_NAME, SelectionUnitArgument.class));

    deserializationConfigMultibinder.addBinding().to(GroupByArgumentDeserializationConfig.class);
    deserializationConfigMultibinder.addBinding().to(IntervalArgumentDeserializationConfig.class);
  }
}
