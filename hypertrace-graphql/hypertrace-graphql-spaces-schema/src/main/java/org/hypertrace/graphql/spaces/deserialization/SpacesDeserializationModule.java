package org.hypertrace.graphql.spaces.deserialization;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;

public class SpacesDeserializationModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<ArgumentDeserializationConfig> deserializationConfigBinder =
        Multibinder.newSetBinder(binder(), ArgumentDeserializationConfig.class);

    deserializationConfigBinder
        .addBinding()
        .to(SpaceConfigRuleDefinitionDeserializationConfig.class);

    deserializationConfigBinder.addBinding().to(SpaceConfigRuleDeserializationConfig.class);

    deserializationConfigBinder
        .addBinding()
        .toInstance(
            ArgumentDeserializationConfig.forPrimitive(
                SpaceRuleIdArgument.ARGUMENT_NAME, SpaceRuleIdArgument.class));
  }
}
