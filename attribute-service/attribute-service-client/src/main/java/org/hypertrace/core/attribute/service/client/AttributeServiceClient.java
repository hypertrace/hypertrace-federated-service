package org.hypertrace.core.attribute.service.client;

import io.grpc.Channel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import org.hypertrace.core.attribute.service.v1.AttributeCreateRequest;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeMetadataFilter;
import org.hypertrace.core.attribute.service.v1.AttributeServiceGrpc;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadataDeleteRequest;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadataUpdateRequest;
import org.hypertrace.core.grpcutils.client.GrpcClientRequestContextUtil;
import org.hypertrace.core.grpcutils.client.RequestContextClientCallCredsProviderFactory;

/** Client for accessing the AttributeService This is a proto based implementation */
public class AttributeServiceClient {
  private final AttributeServiceGrpc.AttributeServiceBlockingStub blockingStub;

  public AttributeServiceClient(Channel channel) {
    this.blockingStub =
        AttributeServiceGrpc.newBlockingStub(channel)
            .withCallCredentials(
                RequestContextClientCallCredsProviderFactory.getClientCallCredsProvider().get());
  }

  private <V> V execute(String tenantId, Callable<V> c) {
    return GrpcClientRequestContextUtil.executeInTenantContext(tenantId, c);
  }

  @Deprecated
  public void create(String tenantId, AttributeCreateRequest request) {
    execute(tenantId, () -> blockingStub.create(request));
  }

  @Deprecated
  public void delete(String tenantId, AttributeMetadataFilter filter) {
    execute(tenantId, () -> blockingStub.delete(filter));
  }

  @Deprecated
  public void updateSourceMetadata(String tenantId, AttributeSourceMetadataUpdateRequest request) {
    execute(tenantId, () -> blockingStub.updateSourceMetadata(request));
  }

  @Deprecated
  public Iterator<AttributeMetadata> findAttributes(
      String tenantId, AttributeMetadataFilter filter) {
    return execute(tenantId, () -> blockingStub.findAttributes(filter));
  }

  @Deprecated
  public void deleteSourceMetadata(String tenantId, AttributeSourceMetadataDeleteRequest req) {
    execute(tenantId, () -> blockingStub.deleteSourceMetadata(req));
  }

  /**
   * If you need to pass down the tenant id use the methods above that pass in the tenant id
   * directly to execute.
   */
  private <V> V execute(Map<String, String> headers, Callable<V> c) {
    return GrpcClientRequestContextUtil.executeWithHeadersContext(headers, c);
  }

  public void create(Map<String, String> headers, AttributeCreateRequest request) {
    execute(headers, () -> blockingStub.create(request));
  }

  public void delete(Map<String, String> headers, AttributeMetadataFilter filter) {
    execute(headers, () -> blockingStub.delete(filter));
  }

  public void updateSourceMetadata(
      Map<String, String> headers, AttributeSourceMetadataUpdateRequest request) {
    execute(headers, () -> blockingStub.updateSourceMetadata(request));
  }

  public Iterator<AttributeMetadata> findAttributes(
      Map<String, String> headers, AttributeMetadataFilter filter) {
    return execute(headers, () -> blockingStub.findAttributes(filter));
  }

  public void deleteSourceMetadata(
      Map<String, String> headers, AttributeSourceMetadataDeleteRequest req) {
    execute(headers, () -> blockingStub.deleteSourceMetadata(req));
  }
}
