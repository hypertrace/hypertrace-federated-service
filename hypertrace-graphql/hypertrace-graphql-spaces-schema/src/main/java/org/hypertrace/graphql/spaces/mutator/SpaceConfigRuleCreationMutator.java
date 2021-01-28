package org.hypertrace.graphql.spaces.mutator;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.hypertrace.core.graphql.common.fetcher.InjectableDataFetcher;
import org.hypertrace.graphql.spaces.dao.SpacesConfigDao;
import org.hypertrace.graphql.spaces.request.SpaceConfigRequestBuilder;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;

public class SpaceConfigRuleCreationMutator extends InjectableDataFetcher<SpaceConfigRule> {

  public SpaceConfigRuleCreationMutator() {
    super(SpaceConfigRuleCreationMutatorImpl.class);
  }

  static final class SpaceConfigRuleCreationMutatorImpl
      implements DataFetcher<CompletableFuture<SpaceConfigRule>> {
    private final SpacesConfigDao configDao;
    private final SpaceConfigRequestBuilder requestBuilder;

    @Inject
    SpaceConfigRuleCreationMutatorImpl(
        SpacesConfigDao configDao, SpaceConfigRequestBuilder requestBuilder) {
      this.configDao = configDao;
      this.requestBuilder = requestBuilder;
    }

    @Override
    public CompletableFuture<SpaceConfigRule> get(DataFetchingEnvironment environment) {
      return this.configDao
          .createRule(
              this.requestBuilder.buildCreationRequest(
                  environment.getContext(), environment.getArguments()))
          .toCompletionStage()
          .toCompletableFuture();
    }
  }
}
