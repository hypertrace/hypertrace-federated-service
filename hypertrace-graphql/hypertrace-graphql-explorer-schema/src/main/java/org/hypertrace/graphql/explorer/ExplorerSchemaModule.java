package org.hypertrace.graphql.explorer;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.spi.schema.GraphQlSchemaFragment;
import org.hypertrace.graphql.explorer.dao.ExplorerDaoModule;
import org.hypertrace.graphql.explorer.deserialization.ExplorerDeserializationModule;
import org.hypertrace.graphql.explorer.request.ExploreRequestModule;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContext;

public class ExplorerSchemaModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), GraphQlSchemaFragment.class)
        .addBinding()
        .to(ExplorerSchemaFragment.class);

    install(new ExplorerDaoModule());
    install(new ExplorerDeserializationModule());
    install(new ExploreRequestModule());

    requireBinding(Key.get(new TypeLiteral<Class<? extends ExplorerContext>>() {}));
  }
}
