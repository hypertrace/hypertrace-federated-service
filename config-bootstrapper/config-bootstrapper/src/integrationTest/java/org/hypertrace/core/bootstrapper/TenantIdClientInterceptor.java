package org.hypertrace.core.bootstrapper;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.hypertrace.core.grpcutils.context.RequestContextConstants;

/**
 * Implementation of {@link ClientInterceptor} which sets the tenantId in RequestContext
 * in every request. Only used for testing because in real setup, envoy proxy does this logic.
 */
class TenantIdClientInterceptor implements ClientInterceptor {
  private final String tenantId;

  public TenantIdClientInterceptor(String tenantId) {
    this.tenantId = tenantId;
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
      CallOptions callOptions, Channel next) {
    return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        headers.put(RequestContextConstants.TENANT_ID_METADATA_KEY, tenantId);

        super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(
            responseListener) {
          @Override
          public void onHeaders(Metadata headers) {
            super.onHeaders(headers);
          }
        }, headers);
      }
    };
  }
}
