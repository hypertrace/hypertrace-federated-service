package org.hypertrace.space.config.service;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Channel;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.reactivex.rxjava3.core.Single;
import java.util.function.Function;
import org.hypertrace.config.service.v1.ConfigServiceGrpc;
import org.hypertrace.config.service.v1.ConfigServiceGrpc.ConfigServiceFutureStub;
import org.hypertrace.core.grpcutils.client.RequestContextClientCallCredsProviderFactory;
import org.hypertrace.core.grpcutils.client.rx.GrpcRxExecutionContext;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.hypertrace.core.grpcutils.server.rx.ServerCallStreamRxObserver;
import org.hypertrace.spaces.config.service.v1.CreateRuleRequest;
import org.hypertrace.spaces.config.service.v1.CreateRuleResponse;
import org.hypertrace.spaces.config.service.v1.DeleteRuleRequest;
import org.hypertrace.spaces.config.service.v1.DeleteRuleResponse;
import org.hypertrace.spaces.config.service.v1.GetRulesRequest;
import org.hypertrace.spaces.config.service.v1.GetRulesResponse;
import org.hypertrace.spaces.config.service.v1.SpacesConfigServiceGrpc.SpacesConfigServiceImplBase;
import org.hypertrace.spaces.config.service.v1.UpdateRuleRequest;
import org.hypertrace.spaces.config.service.v1.UpdateRuleResponse;

public class SpacesConfigServiceImpl extends SpacesConfigServiceImplBase {
  private final ConfigServiceFutureStub configServiceStub;
  private final SpaceConfigRequestValidator requestValidator;
  private final SpaceConfigRequestConverter requestConverter;
  private final SpaceConfigResponseConverter responseConverter;

  public SpacesConfigServiceImpl(Channel configChannel) {
    this.configServiceStub =
        ConfigServiceGrpc.newFutureStub(configChannel)
            .withCallCredentials(
                RequestContextClientCallCredsProviderFactory.getClientCallCredsProvider().get());
    this.requestConverter = new SpaceConfigRequestConverterImpl(new SpaceConfigRuleIdGenerator());
    this.requestValidator = new SpaceConfigRequestValidatorImpl();
    this.responseConverter = new SpaceConfigResponseConverterImpl();
  }

  @Override
  public void createRule(
      CreateRuleRequest request, StreamObserver<CreateRuleResponse> responseObserver) {
    RequestContext context = RequestContext.CURRENT.get();

    this.requestValidator
        .validateCreateRequest(context, request)
        .map(this.requestConverter::convertCreateRequest)
        .flatMap(this.callInContext(context, this.configServiceStub::upsertConfig)::apply)
        .map(this.responseConverter::convertCreateResponse)
        .subscribe(
            new ServerCallStreamRxObserver<>(
                (ServerCallStreamObserver<CreateRuleResponse>) responseObserver));
  }

  @Override
  public void getRules(GetRulesRequest request, StreamObserver<GetRulesResponse> responseObserver) {
    RequestContext context = RequestContext.CURRENT.get();

    this.requestValidator
        .validateGetRequest(context, request)
        .map(this.requestConverter::convertGetRequest)
        .flatMap(this.callInContext(context, this.configServiceStub::getAllConfigs)::apply)
        .map(this.responseConverter::convertGetResponse)
        .subscribe(
            new ServerCallStreamRxObserver<>(
                (ServerCallStreamObserver<GetRulesResponse>) responseObserver));
  }

  @Override
  public void updateRule(
      UpdateRuleRequest request, StreamObserver<UpdateRuleResponse> responseObserver) {
    RequestContext context = RequestContext.CURRENT.get();

    this.requestValidator
        .validateUpdateRequest(context, request)
        .map(this.requestConverter::convertUpdateRequest)
        .flatMap(this.callInContext(context, this.configServiceStub::upsertConfig)::apply)
        .map(this.responseConverter::convertUpdateResponse)
        .subscribe(
            new ServerCallStreamRxObserver<>(
                (ServerCallStreamObserver<UpdateRuleResponse>) responseObserver));
  }

  @Override
  public void deleteRule(
      DeleteRuleRequest request, StreamObserver<DeleteRuleResponse> responseObserver) {
    RequestContext context = RequestContext.CURRENT.get();

    this.requestValidator
        .validateDeleteRequest(context, request)
        .map(this.requestConverter::convertDeleteRequest)
        .flatMap(this.callInContext(context, this.configServiceStub::deleteConfig)::apply)
        .map(this.responseConverter::convertDeleteResponse)
        .subscribe(
            new ServerCallStreamRxObserver<>(
                (ServerCallStreamObserver<DeleteRuleResponse>) responseObserver));
  }

  private <TRequest, TResponse> Function<TRequest, Single<TResponse>> callInContext(
      RequestContext context, Function<TRequest, ListenableFuture<TResponse>> callable) {
    return request ->
        GrpcRxExecutionContext.forContext(context)
            .call(() -> callable.apply(request))
            .flatMap(Single::fromFuture);
  }
}
