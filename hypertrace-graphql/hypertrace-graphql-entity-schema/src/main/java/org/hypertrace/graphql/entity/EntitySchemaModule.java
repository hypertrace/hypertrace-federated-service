package org.hypertrace.graphql.entity;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.spi.schema.GraphQlSchemaFragment;
import org.hypertrace.graphql.entity.dao.EntityDaoModule;
import org.hypertrace.graphql.entity.deserialization.EntityDeserializationModule;
import org.hypertrace.graphql.entity.request.EntityRequestModule;
import org.hypertrace.graphql.entity.schema.EntityType;

public class EntitySchemaModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), GraphQlSchemaFragment.class)
        .addBinding()
        .to(EntitySchemaFragment.class);

    install(new EntityDaoModule());
    install(new EntityDeserializationModule());
    install(new EntityRequestModule());

    requireBinding(Key.get(new TypeLiteral<Class<? extends EntityType>>() {}));
  }
}
