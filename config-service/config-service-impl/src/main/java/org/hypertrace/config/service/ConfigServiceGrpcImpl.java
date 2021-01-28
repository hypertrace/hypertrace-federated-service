package org.hypertrace.config.service;

import static org.hypertrace.config.service.ConfigServiceUtils.DEFAULT_CONTEXT;
import static org.hypertrace.config.service.ConfigServiceUtils.emptyValue;
import static org.hypertrace.config.service.ConfigServiceUtils.filterNull;
import static org.hypertrace.config.service.ConfigServiceUtils.getActualContext;
import static org.hypertrace.config.service.ConfigServiceUtils.merge;

import com.google.protobuf.Value;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hypertrace.config.service.store.ConfigStore;
import org.hypertrace.config.service.v1.ConfigServiceGrpc;
import org.hypertrace.config.service.v1.ContextSpecificConfig;
import org.hypertrace.config.service.v1.DeleteConfigRequest;
import org.hypertrace.config.service.v1.DeleteConfigResponse;
import org.hypertrace.config.service.v1.GetAllConfigsRequest;
import org.hypertrace.config.service.v1.GetAllConfigsResponse;
import org.hypertrace.config.service.v1.GetConfigRequest;
import org.hypertrace.config.service.v1.GetConfigResponse;
import org.hypertrace.config.service.v1.UpsertConfigRequest;
import org.hypertrace.config.service.v1.UpsertConfigResponse;
import org.hypertrace.core.grpcutils.context.RequestContext;

/**
 * Implementation for the gRPC service.
 */
@Slf4j
public class ConfigServiceGrpcImpl extends ConfigServiceGrpc.ConfigServiceImplBase {

  private final ConfigStore configStore;

  public ConfigServiceGrpcImpl(ConfigStore configStore) {
    this.configStore = configStore;
  }

  @Override
  public void upsertConfig(UpsertConfigRequest request,
      StreamObserver<UpsertConfigResponse> responseObserver) {
    try {
      ConfigResource configResource = getConfigResource(request);
      Value existingConfig = configStore.getConfig(configResource);
      Value resultingConfig = merge(existingConfig, request.getConfig());
      configStore.writeConfig(configResource, getUserId(), resultingConfig);
      responseObserver.onNext(UpsertConfigResponse.newBuilder()
          .setConfig(resultingConfig)
          .build());
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Upsert failed for request:{}", request, e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void getConfig(GetConfigRequest request,
      StreamObserver<GetConfigResponse> responseObserver) {
    try {
      Value config = configStore.getConfig(getConfigResource(request));

      // get the configs for the contexts mentioned in the request and merge them in the specified order
      for (String context : request.getContextsList()) {
        Value contextSpecificConfig = configStore.getConfig(getConfigResource(request, context));
        config = merge(config, contextSpecificConfig);
      }

      filterNull(config)
          .map(nonNullConfig -> GetConfigResponse.newBuilder().setConfig(nonNullConfig).build())
          .ifPresentOrElse(
              response -> {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
              },
              () -> responseObserver.onError(Status.NOT_FOUND.asException()));

    } catch (Exception e) {
      log.error("Get config failed for request:{}", request, e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllConfigs(GetAllConfigsRequest request,
      StreamObserver<GetAllConfigsResponse> responseObserver) {
    try {
      List<ContextSpecificConfig> contextSpecificConfigList = configStore
          .getAllConfigs(request.getResourceName(), request.getResourceNamespace(), getTenantId());
      responseObserver.onNext(
          GetAllConfigsResponse.newBuilder().addAllContextSpecificConfigs(contextSpecificConfigList)
              .build());
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Get all configs failed for request:{}", request, e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void deleteConfig(DeleteConfigRequest request,
      StreamObserver<DeleteConfigResponse> responseObserver) {
    try {
      // write an empty config for the specified config resource. This maintains the versioning.
      configStore.writeConfig(getConfigResource(request), getUserId(), emptyValue());
      responseObserver.onNext(DeleteConfigResponse.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Delete config failed for request:{}", request, e);
      responseObserver.onError(e);
    }
  }

  private ConfigResource getConfigResource(UpsertConfigRequest upsertConfigRequest) {
    return new ConfigResource(upsertConfigRequest.getResourceName(),
        upsertConfigRequest.getResourceNamespace(),
        getTenantId(),
        getActualContext(upsertConfigRequest.getContext()));
  }

  private ConfigResource getConfigResource(GetConfigRequest getConfigRequest) {
    return new ConfigResource(getConfigRequest.getResourceName(),
        getConfigRequest.getResourceNamespace(),
        getTenantId(),
        DEFAULT_CONTEXT);
  }

  private ConfigResource getConfigResource(GetConfigRequest getConfigRequest, String context) {
    return new ConfigResource(getConfigRequest.getResourceName(),
        getConfigRequest.getResourceNamespace(),
        getTenantId(),
        getActualContext(context));
  }

  private ConfigResource getConfigResource(DeleteConfigRequest deleteConfigRequest) {
    return new ConfigResource(deleteConfigRequest.getResourceName(),
        deleteConfigRequest.getResourceNamespace(),
        getTenantId(),
        deleteConfigRequest.getContext());
  }

  private String getTenantId() {
    Optional<String> tenantId = RequestContext.CURRENT.get().getTenantId();
    if (tenantId.isEmpty()) {
      throw new IllegalArgumentException("Tenant Id is missing in the request.");
    }
    return tenantId.get();
  }

  // TODO : get the userId from the context
  private String getUserId() {
    return "";
  }
}
