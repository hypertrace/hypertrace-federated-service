package org.hypertrace.graphql.explorer.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.graphql.explorer.dao.ExplorerDao;
import org.hypertrace.graphql.explorer.request.ExploreRequestBuilder;
import org.hypertrace.graphql.explorer.schema.ExploreResultSet;

public class ExplorerFetcher extends InjectableDataFetcher<ExploreResultSet> {

  public ExplorerFetcher() {
    super(ExplorerFetcherImpl.class);
  }

  static final class ExplorerFetcherImpl
      implements DataFetcher<CompletableFuture<ExploreResultSet>> {

    private final ExplorerDao explorerDao;
    private final ExploreRequestBuilder requestBuilder;

    @Inject
    ExplorerFetcherImpl(ExplorerDao explorerDao, ExploreRequestBuilder requestBuilder) {
      this.explorerDao = explorerDao;
      this.requestBuilder = requestBuilder;
    }

    @Override
    public CompletableFuture<ExploreResultSet> get(DataFetchingEnvironment environment) {
      return this.requestBuilder
          .build(
              environment.getContext(), environment.getArguments(), environment.getSelectionSet())
          .flatMap(this.explorerDao::explore)
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
