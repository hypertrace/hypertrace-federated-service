package org.hypertrace.graphql.spaces.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.graphql.spaces.dao.SpacesConfigDao;
import org.hypertrace.graphql.spaces.schema.query.SpaceConfigRuleResultSet;

public class SpaceConfigRuleFetcher extends InjectableDataFetcher<SpaceConfigRuleResultSet> {

  public SpaceConfigRuleFetcher() {
    super(SpaceConfigRuleFetcherImpl.class);
  }

  static final class SpaceConfigRuleFetcherImpl
      implements DataFetcher<CompletableFuture<SpaceConfigRuleResultSet>> {
    private final SpacesConfigDao configDao;

    @Inject
    SpaceConfigRuleFetcherImpl(SpacesConfigDao configDao) {
      this.configDao = configDao;
    }

    @Override
    public CompletableFuture<SpaceConfigRuleResultSet> get(DataFetchingEnvironment environment) {
      return this.configDao
          .getAllRules(environment.getContext())
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
