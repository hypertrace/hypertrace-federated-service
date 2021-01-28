package org.hypertrace.graphql.spaces.mutator;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.graphql.spaces.dao.SpacesConfigDao;
import org.hypertrace.graphql.spaces.request.SpaceConfigRequestBuilder;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;

public class SpaceConfigRuleUpdateMutator extends InjectableDataFetcher<SpaceConfigRule> {

  public SpaceConfigRuleUpdateMutator() {
    super(SpaceConfigRuleUpdateMutatorImpl.class);
  }

  static final class SpaceConfigRuleUpdateMutatorImpl
      implements DataFetcher<CompletableFuture<SpaceConfigRule>> {
    private final SpacesConfigDao configDao;
    private final SpaceConfigRequestBuilder requestBuilder;

    @Inject
    SpaceConfigRuleUpdateMutatorImpl(
        SpacesConfigDao configDao, SpaceConfigRequestBuilder requestBuilder) {
      this.configDao = configDao;
      this.requestBuilder = requestBuilder;
    }

    @Override
    public CompletableFuture<SpaceConfigRule> get(DataFetchingEnvironment environment) {
      return this.configDao
          .updateRule(
              this.requestBuilder.buildUpdateRequest(
                  environment.getContext(), environment.getArguments()))
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
