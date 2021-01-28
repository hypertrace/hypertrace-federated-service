package org.hypertrace.graphql.spaces.mutator;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.graphql.spaces.dao.SpacesConfigDao;
import org.hypertrace.graphql.spaces.request.SpaceConfigRequestBuilder;

public class SpaceConfigRuleDeleteMutator extends InjectableDataFetcher<Boolean> {

  public SpaceConfigRuleDeleteMutator() {
    super(SpaceConfigRuleDeleteMutatorImpl.class);
  }

  static final class SpaceConfigRuleDeleteMutatorImpl
      implements DataFetcher<CompletableFuture<Boolean>> {
    private final SpacesConfigDao configDao;
    private final SpaceConfigRequestBuilder requestBuilder;

    @Inject
    SpaceConfigRuleDeleteMutatorImpl(
        SpacesConfigDao configDao, SpaceConfigRequestBuilder requestBuilder) {
      this.configDao = configDao;
      this.requestBuilder = requestBuilder;
    }

    @Override
    public CompletableFuture<Boolean> get(DataFetchingEnvironment environment) {
      return this.configDao
          .deleteRule(
              this.requestBuilder.buildDeleteRequest(
                  environment.getContext(), environment.getArguments()))
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
