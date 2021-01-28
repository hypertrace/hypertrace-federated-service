package org.hypertrace.graphql.explorer.request;

import graphql.schema.DataFetchingFieldSelectionSet;
import io.reactivex.rxjava3.core.Single;
import java.util.Map;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

public interface ExploreRequestBuilder {
  Single<ExploreRequest> build(
      GraphQlRequestContext context,
      Map<String, Object> arguments,
      DataFetchingFieldSelectionSet selectionSet);
}
