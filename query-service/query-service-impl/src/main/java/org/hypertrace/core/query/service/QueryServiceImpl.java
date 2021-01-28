package org.hypertrace.core.query.service;

import static org.hypertrace.core.query.service.RowChunkingOperator.chunkRows;

import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.hypertrace.core.grpcutils.server.rx.ServerCallStreamRxObserver;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.QueryServiceGrpc;
import org.hypertrace.core.query.service.api.ResultSetChunk;

@Singleton
class QueryServiceImpl extends QueryServiceGrpc.QueryServiceImplBase {
  private final RequestHandlerSelector handlerSelector;
  private final QueryTransformationPipeline queryTransformationPipeline;

  @Inject
  public QueryServiceImpl(
      RequestHandlerSelector handlerSelector,
      QueryTransformationPipeline queryTransformationPipeline) {
    this.handlerSelector = handlerSelector;
    this.queryTransformationPipeline = queryTransformationPipeline;
  }

  @Override
  public void execute(
      QueryRequest originalRequest, StreamObserver<ResultSetChunk> callStreamObserver) {
    Maybe.fromOptional(RequestContext.CURRENT.get().getTenantId())
        .switchIfEmpty(
            Maybe.error(new UnsupportedOperationException("Tenant ID missing in request context")))
        .flatMapObservable(tenantId -> this.transformAndExecute(originalRequest, tenantId))
        .subscribe(
            new ServerCallStreamRxObserver<>(
                (ServerCallStreamObserver<ResultSetChunk>) callStreamObserver));
  }

  private Observable<ResultSetChunk> transformAndExecute(
      QueryRequest originalRequest, String tenantId) {
    return this.queryTransformationPipeline
        .transform(originalRequest, tenantId)
        .flatMapObservable(
            transformedRequest ->
                this.executeTransformedRequest(
                    transformedRequest, new ExecutionContext(tenantId, transformedRequest)));
  }

  private Observable<ResultSetChunk> executeTransformedRequest(
      QueryRequest transformedRequest, ExecutionContext context) {
    return Maybe.fromOptional(this.handlerSelector.select(transformedRequest, context))
        .switchIfEmpty(
            Maybe.error(
                Status.FAILED_PRECONDITION
                    .withDescription("No handler available matching request")
                    .asException()))
        .flatMapObservable(handler -> handler.handleRequest(transformedRequest, context))
        .lift(chunkRows(context.getResultSetMetadata()));
  }
}
