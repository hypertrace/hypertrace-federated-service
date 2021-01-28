package org.hypertrace.graphql.explorer.schema.types;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.typeBuilders.EnumBuilder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLType;
import java.lang.reflect.AnnotatedType;
import javax.inject.Inject;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContext;

public class ExplorerContextDynamicEnum implements TypeFunction {

  private final GraphQLEnumType enumType;

  @Inject
  ExplorerContextDynamicEnum(Class<? extends ExplorerContext> explorerContextImplementationClass) {
    this.enumType =
        new EnumBuilder(new GraphQLObjectInfoRetriever())
            .getEnumBuilder(explorerContextImplementationClass)
            .name(ExplorerContext.TYPE_NAME)
            .build();
  }

  @Override
  public boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType) {
    return aClass == ExplorerContext.class;
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
