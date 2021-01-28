package org.hypertrace.graphql.entity;

import graphql.annotations.processor.typeFunctions.TypeFunction;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.hypertrace.core.graphql.spi.schema.GraphQlSchemaFragment;
import org.hypertrace.graphql.entity.schema.EntitySchema;
import org.hypertrace.graphql.entity.schema.types.EntityTypeDynamicEnum;

class EntitySchemaFragment implements GraphQlSchemaFragment {

  private final EntityTypeDynamicEnum entityTypeDynamicEnum;

  @Inject
  EntitySchemaFragment(EntityTypeDynamicEnum entityTypeDynamicEnum) {
    this.entityTypeDynamicEnum = entityTypeDynamicEnum;
  }

  @Override
  public String fragmentName() {
    return "Entity schema";
  }

  @Override
  public Class<EntitySchema> annotatedQueryClass() {
    return EntitySchema.class;
  }

  @Nonnull
  @Override
  public List<TypeFunction> typeFunctions() {
    return List.of(this.entityTypeDynamicEnum);
  }
}
