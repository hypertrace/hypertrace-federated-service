package org.hypertrace.graphql.spaces.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.graphql.spaces.dao.SpacesDao;
import org.hypertrace.graphql.spaces.schema.query.SpaceResultSet;

public class SpacesFetcher extends InjectableDataFetcher<SpaceResultSet> {

  public SpacesFetcher() {
    super(SpacesFetcherImpl.class);
  }

  static final class SpacesFetcherImpl implements DataFetcher<CompletableFuture<SpaceResultSet>> {
    private final SpacesDao configDao;

    @Inject
    SpacesFetcherImpl(SpacesDao configDao) {
      this.configDao = configDao;
    }

    @Override
    public CompletableFuture<SpaceResultSet> get(DataFetchingEnvironment environment) {
      return this.configDao
          .getAllSpaces(environment.getContext())
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
