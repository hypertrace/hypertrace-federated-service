package org.hypertrace.graphql.spaces.request;

import com.google.inject.AbstractModule;
import org.hypertrace.core.graphql.common.utils.attributes.AttributeScopeStringTranslator;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;

public class SpacesRequestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SpaceConfigRequestBuilder.class).to(SpaceConfigRequestBuilderImpl.class);

    requireBinding(ArgumentDeserializer.class);
    requireBinding(AttributeScopeStringTranslator.class);
  }
}
