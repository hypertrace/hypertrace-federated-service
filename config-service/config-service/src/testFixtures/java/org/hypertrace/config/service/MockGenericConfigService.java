package org.hypertrace.config.service;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.protobuf.Value;
import io.grpc.BindableService;
import io.grpc.Channel;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hypertrace.config.service.v1.ConfigServiceGrpc.ConfigServiceImplBase;
import org.hypertrace.config.service.v1.ContextSpecificConfig;
import org.hypertrace.config.service.v1.ContextSpecificConfig.Builder;
import org.hypertrace.config.service.v1.DeleteConfigRequest;
import org.hypertrace.config.service.v1.DeleteConfigResponse;
import org.hypertrace.config.service.v1.GetAllConfigsRequest;
import org.hypertrace.config.service.v1.GetAllConfigsResponse;
import org.hypertrace.config.service.v1.GetConfigRequest;
import org.hypertrace.config.service.v1.GetConfigResponse;
import org.hypertrace.config.service.v1.UpsertConfigRequest;
import org.hypertrace.config.service.v1.UpsertConfigResponse;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * A mock implementation of the generic config service.
 *
 * <p>Each method must be explicitly requested for mocking to allow strict stubbing.
 */
public class MockGenericConfigService {

  private Server grpcServer;
  private final InProcessServerBuilder serverBuilder;
  private final ManagedChannel configChannel;
  private final RequestContext context = RequestContext.forTenantId("default tenant");
  private final Table<ResourceType, String, Value> currentValues =
      Tables.newCustomTable(new LinkedHashMap<>(), LinkedHashMap::new);

  private final ConfigServiceImplBase mockConfigService =
      Mockito.mock(
          ConfigServiceImplBase.class,
          invocation -> { // Error if unmocked called so we don't hang waiting for streamobserver
            throw new UnsupportedOperationException("Unmocked method invoked");
          });

  public MockGenericConfigService addService(BindableService service) {
    this.serverBuilder.addService(ServerInterceptors.intercept(service, new TestInterceptor()));
    return this;
  }

  public MockGenericConfigService() {
    String uniqueName = InProcessServerBuilder.generateName();
    this.configChannel = InProcessChannelBuilder.forName(uniqueName).directExecutor().build();
    this.serverBuilder =
        InProcessServerBuilder.forName(uniqueName)
            .directExecutor()
            .addService(this.mockConfigService);
  }

  public void start() {
    try {
      this.grpcServer = serverBuilder.build().start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Channel channel() {
    return this.configChannel;
  }

  public void shutdown() {
    this.currentValues.clear();
    this.grpcServer.shutdownNow();
    this.configChannel.shutdownNow();
  }

  public MockGenericConfigService mockUpsert() {
    Mockito.doAnswer(
            invocation -> {
              UpsertConfigRequest request = invocation.getArgument(0, UpsertConfigRequest.class);
              StreamObserver<UpsertConfigResponse> responseStreamObserver =
                  invocation.getArgument(1, StreamObserver.class);
              currentValues.put(
                  ResourceType.of(request.getResourceNamespace(), request.getResourceName()),
                  request.getContext(),
                  request.getConfig());
              responseStreamObserver.onNext(
                  UpsertConfigResponse.newBuilder().setConfig(request.getConfig()).build());
              responseStreamObserver.onCompleted();
              return null;
            })
        .when(this.mockConfigService)
        .upsertConfig(ArgumentMatchers.any(), ArgumentMatchers.any());

    return this;
  }

  public MockGenericConfigService mockGetAll() {
    Mockito.doAnswer(
            invocation -> {
              StreamObserver<GetAllConfigsResponse> responseStreamObserver =
                  invocation.getArgument(1, StreamObserver.class);
              GetAllConfigsRequest request = invocation.getArgument(0, GetAllConfigsRequest.class);
              GetAllConfigsResponse response =
                  currentValues
                      .row(
                          ResourceType.of(
                              request.getResourceNamespace(), request.getResourceName()))
                      .values()
                      .stream()
                      .map(value -> ContextSpecificConfig.newBuilder().setConfig(value))
                      .map(Builder::build)
                      .collect(
                          Collectors.collectingAndThen(
                              Collectors.toList(),
                              list ->
                                  GetAllConfigsResponse.newBuilder()
                                      .addAllContextSpecificConfigs(list)
                                      .build()));

              responseStreamObserver.onNext(response);
              responseStreamObserver.onCompleted();
              return null;
            })
        .when(this.mockConfigService)
        .getAllConfigs(ArgumentMatchers.any(), ArgumentMatchers.any());

    return this;
  }

  public MockGenericConfigService mockDelete() {
    Mockito.doAnswer(
            invocation -> {
              DeleteConfigRequest request = invocation.getArgument(0, DeleteConfigRequest.class);
              StreamObserver<DeleteConfigResponse> responseStreamObserver =
                  invocation.getArgument(1, StreamObserver.class);

              currentValues.remove(
                  ResourceType.of(request.getResourceNamespace(), request.getResourceName()),
                  request.getContext());
              responseStreamObserver.onNext(DeleteConfigResponse.getDefaultInstance());
              responseStreamObserver.onCompleted();
              return null;
            })
        .when(this.mockConfigService)
        .deleteConfig(ArgumentMatchers.any(), ArgumentMatchers.any());

    return this;
  }

  public MockGenericConfigService mockGet() {
    Mockito.doAnswer(
            invocation -> {
              GetConfigRequest request = invocation.getArgument(0, GetConfigRequest.class);
              StreamObserver<GetConfigResponse> responseStreamObserver =
                  invocation.getArgument(1, StreamObserver.class);

              Value mergedValue =
                  request.getContextsList().stream()
                      .map(
                          context ->
                              this.currentValues.get(
                                  ResourceType.of(
                                      request.getResourceNamespace(), request.getResourceName()),
                                  context))
                      .filter(Objects::nonNull)
                      .collect(
                          Collectors.collectingAndThen(Collectors.toList(), this::mergeValues));

              responseStreamObserver.onNext(
                  GetConfigResponse.newBuilder().setConfig(mergedValue).build());
              responseStreamObserver.onCompleted();
              return null;
            })
        .when(this.mockConfigService)
        .getConfig(ArgumentMatchers.any(), ArgumentMatchers.any());

    return this;
  }

  private Value mergeValues(List<Value> values) {
    return values.stream().reduce(Value.getDefaultInstance(), ConfigServiceUtils::merge);
  }

  private class TestInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
      Context ctx = Context.current().withValue(RequestContext.CURRENT, context);
      return Contexts.interceptCall(ctx, call, headers, next);
    }
  }

  @lombok.Value(staticConstructor = "of")
  private static class ResourceType {
    String namespace;
    String name;
  }
}
