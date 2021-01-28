package org.hypertrace.graphql.entity.deserialization;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.graphql.entity.schema.argument.EntityScopeArgument;
import org.hypertrace.graphql.entity.schema.argument.EntityTypeArgument;
import org.hypertrace.graphql.entity.schema.argument.IncludeInactiveArgument;
import org.hypertrace.graphql.entity.schema.argument.NeighborEntityScopeArgument;
import org.hypertrace.graphql.entity.schema.argument.NeighborEntityTypeArgument;

public class EntityDeserializationModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ArgumentDeserializationConfig> deserializationConfig =
        Multibinder.newSetBinder(binder(), ArgumentDeserializationConfig.class);

    deserializationConfig
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                EntityTypeArgument.ARGUMENT_NAME, EntityTypeArgument.class));
    deserializationConfig
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                EntityScopeArgument.ARGUMENT_NAME, EntityScopeArgument.class));
    deserializationConfig
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                NeighborEntityTypeArgument.ARGUMENT_NAME, NeighborEntityTypeArgument.class));
    deserializationConfig
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                NeighborEntityScopeArgument.ARGUMENT_NAME, NeighborEntityScopeArgument.class));
    deserializationConfig
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                IncludeInactiveArgument.ARGUMENT_NAME,
                IncludeInactiveArgument.class));
  }
}
