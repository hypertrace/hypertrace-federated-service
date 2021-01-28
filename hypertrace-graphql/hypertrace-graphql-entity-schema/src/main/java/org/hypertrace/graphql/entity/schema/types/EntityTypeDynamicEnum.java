package org.hypertrace.graphql.entity.schema.types;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.typeBuilders.EnumBuilder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLType;
import java.lang.reflect.AnnotatedType;
import javax.inject.Inject;
import org.hypertrace.graphql.entity.schema.EntityType;

public class EntityTypeDynamicEnum implements TypeFunction {

  private final GraphQLEnumType enumType;

  @Inject
  EntityTypeDynamicEnum(Class<? extends EntityType> entityTypeImplementationClass) {
    this.enumType =
        new EnumBuilder(new GraphQLObjectInfoRetriever())
            .getEnumBuilder(entityTypeImplementationClass)
            .name(EntityType.TYPE_NAME)
            .build();
  }

  @Override
  public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
    return aClass == EntityType.class;
  }

  @Override
  public GraphQLType buildType(
      boolean input,
      Class<?> aClass,
      AnnotatedType annotatedType,
      ProcessingElementsContainer container) {
    return this.enumType;
  }
}
