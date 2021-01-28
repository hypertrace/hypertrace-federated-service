package org.hypertrace.graphql.spaces;

import javax.annotation.Nullable;
import org.hypertrace.core.graphql.spi.schema.GraphQlSchemaFragment;
import org.hypertrace.graphql.spaces.schema.mutation.SpacesMutationSchema;
import org.hypertrace.graphql.spaces.schema.query.SpacesQuerySchema;

class SpacesSchemaFragment implements GraphQlSchemaFragment {

  @Override
  public String fragmentName() {
    return "Spaces schema";
  }

  @Override
  public Class<SpacesQuerySchema> annotatedQueryClass() {
    return SpacesQuerySchema.class;
  }

  @Nullable
  @Override
  public Class<?> annotatedMutationClass() {
    return SpacesMutationSchema.class;
  }
}
