package org.hypertrace.graphql.spaces;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.spi.schema.GraphQlSchemaFragment;
import org.hypertrace.graphql.spaces.dao.SpacesDaoModule;
import org.hypertrace.graphql.spaces.deserialization.SpacesDeserializationModule;
import org.hypertrace.graphql.spaces.request.SpacesRequestModule;

public class SpacesSchemaModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), GraphQlSchemaFragment.class)
        .addBinding()
        .to(SpacesSchemaFragment.class);

    install(new SpacesDaoModule());
    install(new SpacesRequestModule());
    install(new SpacesDeserializationModule());
  }
}
