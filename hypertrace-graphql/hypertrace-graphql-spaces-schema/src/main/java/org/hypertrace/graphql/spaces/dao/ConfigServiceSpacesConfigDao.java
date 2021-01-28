package org.hypertrace.graphql.spaces.dao;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.grpc.CallCredentials;
import io.reactivex.rxjava3.core.Single;
import javax.inject.Inject;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.graphql.config.HypertraceGraphQlServiceConfig;
import org.hypertrace.graphql.spaces.request.SpaceConfigRuleCreationRequest;
import org.hypertrace.graphql.spaces.request.SpaceConfigRuleDeleteRequest;
import org.hypertrace.graphql.spaces.request.SpaceConfigRuleUpdateRequest;
import org.hypertrace.graphql.spaces.schema.query.SpaceConfigRuleResultSet;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;
import org.hypertrace.spaces.config.service.v1.SpacesConfigServiceGrpc;
import org.hypertrace.spaces.config.service.v1.SpacesConfigServiceGrpc.SpacesConfigServiceFutureStub;

class ConfigServiceSpacesConfigDao implements SpacesConfigDao {

  private static final int DEFAULT_DEADLINE_SEC = 10;
  private final SpacesConfigServiceFutureStub spaceConfigServiceStub;
  private final GraphQlGrpcContextBuilder grpcContextBuilder;
  private final SpaceConfigRulesRequestConverter requestConverter;
  private final SpaceConfigRulesResponseConverter responseConverter;

  @Inject
  ConfigServiceSpacesConfigDao(
      HypertraceGraphQlServiceConfig serviceConfig,
      CallCredentials credentials,
      GraphQlGrpcContextBuilder grpcContextBuilder,
      GrpcChannelRegistry grpcChannelRegistry,
      SpaceConfigRulesRequestConverter requestConverter,
      SpaceConfigRulesResponseConverter responseConverter) {
    this.grpcContextBuilder = grpcContextBuilder;
    this.requestConverter = requestConverter;
    this.responseConverter = responseConverter;

    this.spaceConfigServiceStub =
        SpacesConfigServiceGrpc.newFutureStub(
                grpcChannelRegistry.forAddress(
                    serviceConfig.getConfigServiceHost(), serviceConfig.getConfigServicePort()))
            .withCallCredentials(credentials);
  }

  @Override
  public Single<SpaceConfigRuleResultSet> getAllRules(GraphQlRequestContext requestContext) {
    return Single.fromFuture(
            this.grpcContextBuilder
                .build(requestContext)
                .callInContext(
                    () ->
                        this.spaceConfigServiceStub
                            .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                            .getRules(this.requestConverter.convertGetRequest())))
        .flatMap(this.responseConverter::convertGetResponse);
  }

  @Override
  public Single<SpaceConfigRule> createRule(SpaceConfigRuleCreationRequest request) {
    return Single.fromFuture(
            this.grpcContextBuilder
                .build(request.context())
                .callInContext(
                    () ->
                        this.spaceConfigServiceStub
                            .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                            .createRule(this.requestConverter.convertCreationRequest(request))))
        .flatMap(this.responseConverter::convertRule);
  }

  @Override
  public Single<SpaceConfigRule> updateRule(SpaceConfigRuleUpdateRequest request) {
    return Single.fromFuture(
            this.grpcContextBuilder
                .build(request.context())
                .callInContext(
                    () ->
                        this.spaceConfigServiceStub
                            .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                            .updateRule(this.requestConverter.convertUpdateRequest(request))))
        .flatMap(this.responseConverter::convertRule);
  }

  @Override
  public Single<Boolean> deleteRule(SpaceConfigRuleDeleteRequest request) {
    return Single.fromFuture(
            this.grpcContextBuilder
                .build(request.context())
                .callInContext(
                    () ->
                        this.spaceConfigServiceStub
                            .withDeadlineAfter(DEFAULT_DEADLINE_SEC, SECONDS)
                            .deleteRule(this.requestConverter.convertDeleteRequest(request))))
        .flatMap(unusedResponse -> this.responseConverter.buildDeleteResponse());
  }
}
