package org.hypertrace.graphql.entity.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.graphql.entity.dao.EntityDao;
import org.hypertrace.graphql.entity.request.EntityRequestBuilder;
import org.hypertrace.graphql.entity.schema.EntityResultSet;

public class EntityFetcher extends InjectableDataFetcher<EntityResultSet> {

  public EntityFetcher() {
    super(EntityFetcherImpl.class);
  }

  static final class EntityFetcherImpl implements DataFetcher<CompletableFuture<EntityResultSet>> {
    private final EntityRequestBuilder requestBuilder;
    private final EntityDao entityDao;

    @Inject
    EntityFetcherImpl(EntityRequestBuilder requestBuilder, EntityDao entityDao) {
      this.requestBuilder = requestBuilder;
      this.entityDao = entityDao;
    }

    @Override
    public CompletableFuture<EntityResultSet> get(DataFetchingEnvironment environment) {
      return this.requestBuilder
          .build(
              environment.getContext(), environment.getArguments(), environment.getSelectionSet())
          .flatMap(this.entityDao::getEntities)
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
