package org.hypertrace.core.query.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import io.grpc.Context;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.hypertrace.core.query.service.api.ColumnIdentifier;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.Function;
import org.hypertrace.core.query.service.api.LiteralConstant;
import org.hypertrace.core.query.service.api.Operator;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.QueryRequest.Builder;
import org.hypertrace.core.query.service.api.QueryServiceGrpc;
import org.hypertrace.core.query.service.api.QueryServiceGrpc.QueryServiceBlockingStub;
import org.hypertrace.core.query.service.api.ResultSetChunk;
import org.hypertrace.core.query.service.api.ResultSetMetadata;
import org.hypertrace.core.query.service.api.Row;
import org.hypertrace.core.query.service.api.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class QueryServiceImplTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryServiceImplTest.class);

  @Mock ServerCallStreamObserver<ResultSetChunk> mockObserver;
  @Mock RequestContext mockContext;

  @Test
  void propagatesErrorIfNoTenantId() {
    QueryRequest originalRequest = QueryRequest.getDefaultInstance();
    RequestHandlerSelector mockSelector = mock(RequestHandlerSelector.class);
    QueryTransformationPipeline mockTransformationPipeline =
        mock(QueryTransformationPipeline.class);
    when(mockContext.getTenantId()).thenReturn(Optional.empty());
    Context.current()
        .withValue(RequestContext.CURRENT, mockContext)
        .run(
            () ->
                new QueryServiceImpl(mockSelector, mockTransformationPipeline)
                    .execute(originalRequest, mockObserver));
    verify(mockObserver).setOnCancelHandler(any());
    verify(mockObserver).onError(any(UnsupportedOperationException.class));
    verifyNoMoreInteractions(mockObserver);
  }

  @Test
  void propagatesErrorIfNoMatchingHandler() {
    QueryRequest originalRequest = QueryRequest.getDefaultInstance();
    RequestHandlerSelector mockSelector = mock(RequestHandlerSelector.class);
    when(mockSelector.select(same(originalRequest), any())).thenReturn(Optional.empty());
    QueryTransformationPipeline mockTransformationPipeline =
        mock(QueryTransformationPipeline.class);
    when(mockContext.getTenantId()).thenReturn(Optional.of("test-tenant"));
    when(mockTransformationPipeline.transform(originalRequest, "test-tenant"))
        .thenReturn(Single.just(originalRequest));
    Context.current()
        .withValue(RequestContext.CURRENT, mockContext)
        .run(
            () ->
                new QueryServiceImpl(mockSelector, mockTransformationPipeline)
                    .execute(originalRequest, mockObserver));

    verify(mockObserver).setOnCancelHandler(any());
    verify(mockObserver).onError(any(StatusException.class));
    verifyNoMoreInteractions(mockObserver);
  }

  @Test
  void invokesHandlerAndPropagatesResults() {
    QueryRequest originalRequest = QueryRequest.getDefaultInstance();
    RequestHandlerSelector mockSelector = mock(RequestHandlerSelector.class);
    RequestHandler mockHandler = mock(RequestHandler.class);
    Row mockRow = Row.getDefaultInstance();
    when(mockHandler.handleRequest(eq(originalRequest), any(ExecutionContext.class)))
        .thenReturn(Observable.just(mockRow));
    when(mockSelector.select(same(originalRequest), any())).thenReturn(Optional.of(mockHandler));
    QueryTransformationPipeline mockTransformationPipeline =
        mock(QueryTransformationPipeline.class);
    when(mockContext.getTenantId()).thenReturn(Optional.of("test-tenant"));
    when(mockTransformationPipeline.transform(originalRequest, "test-tenant"))
        .thenReturn(Single.just(originalRequest));
    Context.current()
        .withValue(RequestContext.CURRENT, mockContext)
        .run(
            () ->
                new QueryServiceImpl(mockSelector, mockTransformationPipeline)
                    .execute(originalRequest, mockObserver));

    ResultSetChunk expectedChunk =
        ResultSetChunk.newBuilder()
            .setChunkId(0)
            .setIsLastChunk(true)
            .addRow(mockRow)
            .setResultSetMetadata(ResultSetMetadata.getDefaultInstance())
            .build();

    verify(mockObserver).setOnCancelHandler(any());
    verify(mockObserver).onNext(expectedChunk);
    verify(mockObserver).onCompleted();
    verifyNoMoreInteractions(mockObserver);
  }

  // works with query service running at localhost
  @Disabled
  public void testGrpc() {
    ManagedChannel managedChannel =
        ManagedChannelBuilder.forAddress("localhost", 8090).usePlaintext().build();
    QueryServiceBlockingStub QueryServiceBlockingStub =
        QueryServiceGrpc.newBlockingStub(managedChannel);

    ArrayList<QueryRequest> queryRequests =
        Lists.newArrayList(
            buildSimpleQuery(),
            buildAggQuery(),
            buildGroupByAggQuery(),
            buildGroupByTimeRollupAggQuery());

    for (QueryRequest queryRequest : queryRequests) {
      LOGGER.info("Trying to send request {}", queryRequest);
      Iterator<ResultSetChunk> resultSetChunkIterator =
          QueryServiceBlockingStub.withDeadline(Deadline.after(15, TimeUnit.SECONDS))
              .execute(queryRequest);
      LOGGER.info("Got response back: {}", resultSetChunkIterator);
      while (resultSetChunkIterator.hasNext()) {
        LOGGER.info("{}", resultSetChunkIterator.next());
      }
    }
  }

  @Disabled
  public void testGrpcMap() {
    ManagedChannel managedChannel =
        ManagedChannelBuilder.forAddress("localhost", 8090).usePlaintext().build();
    QueryServiceBlockingStub QueryServiceBlockingStub =
        QueryServiceGrpc.newBlockingStub(managedChannel);

    ArrayList<QueryRequest> queryRequests = Lists.newArrayList(buildSimpleMapQuery());

    for (QueryRequest queryRequest : queryRequests) {
      LOGGER.info("Trying to send request {}", queryRequest);
      Iterator<ResultSetChunk> resultSetChunkIterator =
          QueryServiceBlockingStub.withDeadline(Deadline.after(25, TimeUnit.SECONDS))
              .execute(queryRequest);
      LOGGER.info("Got response back: {}", resultSetChunkIterator);
      while (resultSetChunkIterator.hasNext()) {
        LOGGER.info(" Result {}", resultSetChunkIterator.next());
      }
    }
  }

  private QueryRequest buildSimpleQuery() {
    Builder builder = QueryRequest.newBuilder();
    ColumnIdentifier spanId = ColumnIdentifier.newBuilder().setColumnName("EVENT.id").build();
    builder.addSelection(Expression.newBuilder().setColumnIdentifier(spanId).build());

    Filter startTimeFilter =
        createTimeFilter(
            "EVENT.start_time_millis",
            Operator.GT,
            System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Filter endTimeFilter =
        createTimeFilter("EVENT.end_time_millis", Operator.LT, System.currentTimeMillis());

    Filter andFilter =
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(startTimeFilter)
            .addChildFilter(endTimeFilter)
            .build();
    builder.setFilter(andFilter);

    return builder.build();
  }

  private QueryRequest buildGroupByAggQuery() {
    Builder builder = QueryRequest.newBuilder();
    builder.addAggregation(QueryRequestBuilderUtils.createCountByColumnSelection("EVENT.id"));

    Filter startTimeFilter =
        createTimeFilter(
            "EVENT.start_time_millis",
            Operator.GT,
            System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Filter endTimeFilter =
        createTimeFilter("EVENT.end_time_millis", Operator.LT, System.currentTimeMillis());

    Filter andFilter =
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(startTimeFilter)
            .addChildFilter(endTimeFilter)
            .build();
    builder.setFilter(andFilter);

    builder.addGroupBy(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("EVENT.displaySpanName").build()));
    return builder.build();
  }

  private QueryRequest buildGroupByTimeRollupAggQuery() {
    Builder builder = QueryRequest.newBuilder();
    builder.addAggregation(QueryRequestBuilderUtils.createCountByColumnSelection("EVENT.id"));

    Filter startTimeFilter =
        createTimeFilter(
            "EVENT.start_time_millis",
            Operator.GT,
            System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Filter endTimeFilter =
        createTimeFilter("EVENT.end_time_millis", Operator.LT, System.currentTimeMillis());

    Filter andFilter =
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(startTimeFilter)
            .addChildFilter(endTimeFilter)
            .build();
    builder.setFilter(andFilter);

    Function groupByTimeUdf =
        Function.newBuilder()
            .setFunctionName("dateTimeConvert")
            .addArguments(
                Expression.newBuilder()
                    .setColumnIdentifier(
                        ColumnIdentifier.newBuilder()
                            .setColumnName("EVENT.start_time_millis")
                            .build()))
            .addArguments(
                Expression.newBuilder()
                    .setLiteral(
                        LiteralConstant.newBuilder()
                            .setValue(
                                Value.newBuilder().setString("1:MILLISECONDS:EPOCH").build())))
            .addArguments(
                Expression.newBuilder()
                    .setLiteral(
                        LiteralConstant.newBuilder()
                            .setValue(
                                Value.newBuilder().setString("1:MILLISECONDS:EPOCH").build())))
            .addArguments(
                Expression.newBuilder()
                    .setLiteral(
                        LiteralConstant.newBuilder()
                            .setValue(Value.newBuilder().setString("30:SECONDS").build())))
            .build();
    builder.addGroupBy(Expression.newBuilder().setFunction(groupByTimeUdf).build());
    return builder.build();
  }

  private QueryRequest buildSimpleMapQuery() {
    Builder builder = QueryRequest.newBuilder();
    ColumnIdentifier spanId =
        ColumnIdentifier.newBuilder().setColumnName("EVENT.id").setAlias("SpanIds").build();
    builder.addSelection(Expression.newBuilder().setColumnIdentifier(spanId).build());
    builder.addSelection(createSelection("EVENT.end_time_millis"));
    builder.addSelection(createSelection("EVENT.displaySpanName"));
    builder.addSelection(createSelection("EVENT.attributes.request_body"));
    builder.addSelection(createSelection("EVENT.attributes.protocol_name"));
    builder.addSelection(createSelection("EVENT.attributes.request_headers"));
    builder.addSelection(createSelection("EVENT.attributes.response_headers"));

    builder.addSelection(createSelection("EVENT.start_time_millis"));
    builder.addSelection(createSelection("EVENT.metrics.duration_millis"));
    builder.addSelection(createSelection("Service.name"));
    builder.addSelection(createSelection("EVENT.attributes.response_body"));
    builder.addSelection(createSelection("EVENT.attributes.parent_span_id"));

    Filter startTimeFilter =
        createTimeFilter(
            "EVENT.start_time_millis",
            Operator.GT,
            System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Filter endTimeFilter =
        createTimeFilter("EVENT.end_time_millis", Operator.LT, System.currentTimeMillis());

    Filter andFilter =
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(startTimeFilter)
            .addChildFilter(endTimeFilter)
            .build();

    builder.setFilter(andFilter);

    return builder.build();
  }

  private QueryRequest buildAggQuery() {
    Builder builder = QueryRequest.newBuilder();
    Function maxStartTime =
        Function.newBuilder()
            .setFunctionName("MAX")
            .addArguments(
                Expression.newBuilder()
                    .setColumnIdentifier(
                        ColumnIdentifier.newBuilder()
                            .setColumnName("EVENT.start_time_millis")
                            .build()))
            .setAlias("MAX_start_time_millis")
            .build();

    builder.addSelection(createSelection("EVENT.attributes.request_headers"));
    builder.addSelection(Expression.newBuilder().setFunction(maxStartTime).build());

    Filter startTimeFilter =
        createTimeFilter(
            "EVENT.start_time_millis",
            Operator.GT,
            System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Filter endTimeFilter =
        createTimeFilter("EVENT.end_time_millis", Operator.LT, System.currentTimeMillis());

    Filter andFilter =
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(startTimeFilter)
            .addChildFilter(endTimeFilter)
            .build();
    builder.setFilter(andFilter);

    return builder.build();
  }

  private Expression createSelection(String colName) {
    return Expression.newBuilder()
        .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName(colName))
        .build();
  }

  private Filter createTimeFilter(String columnName, Operator op, long value) {

    ColumnIdentifier startTimeColumn =
        ColumnIdentifier.newBuilder().setColumnName(columnName).build();
    Expression lhs = Expression.newBuilder().setColumnIdentifier(startTimeColumn).build();

    // TODO: Why is this not LONG
    LiteralConstant constant =
        LiteralConstant.newBuilder()
            .setValue(Value.newBuilder().setString(String.valueOf(value)).build())
            .build();
    Expression rhs = Expression.newBuilder().setLiteral(constant).build();
    return Filter.newBuilder().setLhs(lhs).setOperator(op).setRhs(rhs).build();
  }

  private Filter createStringColumnFilter(String columnName, Operator op, String value) {
    ColumnIdentifier column = ColumnIdentifier.newBuilder().setColumnName(columnName).build();
    Expression lhs = Expression.newBuilder().setColumnIdentifier(column).build();

    LiteralConstant constant =
        LiteralConstant.newBuilder()
            .setValue(Value.newBuilder().setString(String.valueOf(value)).build())
            .build();
    Expression rhs = Expression.newBuilder().setLiteral(constant).build();
    return Filter.newBuilder().setLhs(lhs).setOperator(op).setRhs(rhs).build();
  }
}
