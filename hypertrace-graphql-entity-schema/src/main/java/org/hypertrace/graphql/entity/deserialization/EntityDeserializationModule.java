package org.hypertrace.graphql.entity.deserialization;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.graphql.entity.schema.argument.EntityTypeArgument;
import org.hypertrace.graphql.entity.schema.argument.NeighborEntityTypeArgument;

public class EntityDeserializationModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder<ArgumentDeserializationConfig> deserializationConfigMultibinder =
        Multibinder.newSetBinder(binder(), ArgumentDeserializationConfig.class);

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                EntityTypeArgument.ARGUMENT_NAME, EntityTypeArgument.class));

    deserializationConfigMultibinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                NeighborEntityTypeArgument.ARGUMENT_NAME, NeighborEntityTypeArgument.class));
  }
}
