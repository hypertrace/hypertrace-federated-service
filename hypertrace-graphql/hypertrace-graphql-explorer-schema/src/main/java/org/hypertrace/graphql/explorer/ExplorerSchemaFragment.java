package org.hypertrace.graphql.explorer;

import graphql.annotations.processor.typeFunctions.TypeFunction;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.hypertrace.core.graphql.spi.schema.GraphQlSchemaFragment;
import org.hypertrace.graphql.explorer.schema.ExplorerSchema;
import org.hypertrace.graphql.explorer.schema.types.ExplorerContextDynamicEnum;

class ExplorerSchemaFragment implements GraphQlSchemaFragment {

  private final ExplorerContextDynamicEnum explorerContextDynamicEnum;

  @Inject
  ExplorerSchemaFragment(ExplorerContextDynamicEnum explorerContextDynamicEnum) {
    this.explorerContextDynamicEnum = explorerContextDynamicEnum;
  }

  @Override
  public String fragmentName() {
    return "Explorer schema";
  }

  @Override
  public Class<ExplorerSchema> annotatedQueryClass() {
    return ExplorerSchema.class;
  }

  @Nonnull
  @Override
  public List<TypeFunction> typeFunctions() {
    return List.of(this.explorerContextDynamicEnum);
  }
}
