package org.hypertrace.core.query.service.pinot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.collections.Iterables.firstOf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjava3.core.Observable;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.pinot.client.ResultSet;
import org.apache.pinot.client.ResultSetGroup;
import org.hypertrace.core.query.service.ExecutionContext;
import org.hypertrace.core.query.service.QueryCost;
import org.hypertrace.core.query.service.QueryRequestBuilderUtils;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.LiteralConstant;
import org.hypertrace.core.query.service.api.Operator;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.Row;
import org.hypertrace.core.query.service.api.Value;
import org.hypertrace.core.query.service.api.ValueType;
import org.hypertrace.core.query.service.pinot.PinotClientFactory.PinotClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PinotBasedRequestHandlerTest {
  // Test subject
  private PinotBasedRequestHandler pinotBasedRequestHandler;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Config serviceConfig =
      ConfigFactory.parseURL(
              Objects.requireNonNull(
                  QueryRequestToPinotSQLConverterTest.class
                      .getClassLoader()
                      .getResource("application.conf")))
          .getConfig("service.config");

  @BeforeEach
  public void setUp() {
    // Mocks
    PinotClientFactory pinotClientFactoryMock = mock(PinotClientFactory.class);
    ResultSetTypePredicateProvider resultSetTypePredicateProviderMock = mock(
        ResultSetTypePredicateProvider.class);

    Config handlerConfig = firstOf(serviceConfig.getConfigList("queryRequestHandlersConfig"));
    pinotBasedRequestHandler =
        new PinotBasedRequestHandler(
            handlerConfig.getString("name"),
            handlerConfig.getConfig("requestHandlerInfo"),
            resultSetTypePredicateProviderMock,
            pinotClientFactoryMock);

    // Test ResultTableResultSet result set format parsing
    when(resultSetTypePredicateProviderMock.isSelectionResultSetType(any(ResultSet.class)))
        .thenReturn(false);
    when(resultSetTypePredicateProviderMock.isResultTableResultSetType(any(ResultSet.class)))
        .thenReturn(true);
  }

  @Test
  public void testInit() {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      new PinotBasedRequestHandler(
          config.getString("name"), config.getConfig("requestHandlerInfo"));
    }
  }

  @Test
  public void testInitFailure() {
    Assertions.assertThrows(RuntimeException.class, () -> {
      Config config = ConfigFactory.parseMap(Map.of("name", "test",
          "requestHandlerInfo", Map.of()));
      new PinotBasedRequestHandler(
          config.getString("name"), config.getConfig("requestHandlerInfo"));
    });
  }

  @Test
  public void testCanHandle() {
    for (Config config: serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"), config.getConfig("requestHandlerInfo"));

      // Verify that the traces handler can traces query.
      if (config.getString("name").equals("trace-view-handler")) {
        QueryRequest request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("Trace.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("Trace.duration_millis"))
            .setFilter(QueryRequestBuilderUtils.createFilter("Trace.end_time_millis", Operator.GT,
                QueryRequestBuilderUtils.createLongLiteralValueExpression(10)))
            .build();
        ExecutionContext context = new ExecutionContext("__default", request);
        QueryCost cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);
      }
    }
  }

  @Test
  public void testCanHandleNegativeCase() {
    for (Config config: serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      PinotBasedRequestHandler handler = new PinotBasedRequestHandler(config.getString("name"), config.getConfig("requestHandlerInfo"));
      // Verify that the traces handler can traces query.
      if (config.getString("name").equals("trace-view-handler")) {
        QueryRequest request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("Trace.does_not_exist"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("Trace.end_time_millis"))
            .addGroupBy(QueryRequestBuilderUtils.createColumnExpression("Trace.tags"))
            .build();
        ExecutionContext context = new ExecutionContext("__default", request);
        QueryCost cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);
      }
    }
  }

  @Test
  public void testCanHandleWithInViewFilter() {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"), config.getConfig("requestHandlerInfo"));

      // Verify that the span event view handler can handle the query which has the filter
      // on the column which has a view filter.
      if (config.getString("name").equals("span-event-view-handler")) {
        QueryRequest request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
                .addChildFilter(QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                    QueryRequestBuilderUtils.createLongLiteralValueExpression(System.currentTimeMillis()))).build())
            .build();
        ExecutionContext context = new ExecutionContext("__default", request);
        QueryCost cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Entry span "false" filter.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "false"))
            .build();
        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Wrong value in the filter.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "dummy"))
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Unsupported operator in the query filter.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.LIKE,
                QueryRequestBuilderUtils.createStringLiteralValueExpression("dummy")))
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Any query without filter should not be handled.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .build();

        context = new ExecutionContext("__default", request);
        QueryCost negativeCost = handler.canHandle(request, context);
        Assertions.assertFalse(negativeCost.getCost() >= 0.0d && negativeCost.getCost() < 1.0d);
      }
    }
  }

  @Test
  public void testCanHandleWithEqViewFilter() {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"), config.getConfig("requestHandlerInfo"));

      // Verify that the entry span view handler can handle the query which has the filter
      // on the column which has a view filter.
      if (config.getString("name").equals("entry-span-view-handler")) {
        QueryRequest request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(
                    QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                        QueryRequestBuilderUtils
                            .createLongLiteralValueExpression(System.currentTimeMillis()))).build())
            .build();

        ExecutionContext context = new ExecutionContext("__default", request);
        QueryCost cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Positive case with boolean filter.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                        Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                            Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL)))
                            .build()))
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                        QueryRequestBuilderUtils
                            .createLongLiteralValueExpression(System.currentTimeMillis()))).build())
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case with boolean filter but 'OR' operation.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.OR)
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                        Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                            Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL)))
                            .build()))
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                        QueryRequestBuilderUtils
                            .createLongLiteralValueExpression(System.currentTimeMillis()))).build())
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case with a complex filter but 'OR' at the root level, hence shouldn't match.
        Filter filter = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                    Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL))).build()))
            .addChildFilter(
                QueryRequestBuilderUtils.createFilter("EVENT.duration_millis", Operator.GT,
                    QueryRequestBuilderUtils.createLongLiteralValueExpression(10L))).build();
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.OR)
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                        Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                            Value.newBuilder().setBoolean(false).setValueType(ValueType.BOOL)))
                            .build()))
                .addChildFilter(filter))
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Value in query filter is different from the value in view filter
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "false"))
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Unsupported operator in the query filter.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.IN,
                QueryRequestBuilderUtils.createStringLiteralValueExpression("dummy")))
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Any query without filter should not be handled.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .build();
        context = new ExecutionContext("__default", request);
        QueryCost negativeCost = handler.canHandle(request, context);
        Assertions.assertFalse(negativeCost.getCost() >= 0.0d && negativeCost.getCost() < 1.0d);
      }
    }
  }

  @Test
  public void testCanHandleWithMultipleViewFilters() {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"), config.getConfig("requestHandlerInfo"));

      // Verify that the entry span view handler can handle the query which has the filter
      // on the column which has a view filter.
      if (config.getString("name").equals("error-entry-span-view-handler")) {
        // Positive case, straight forward.
        QueryRequest request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(
                    QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
                .addChildFilter(
                    QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", "401")))
            .build();

        ExecutionContext executionContext = new ExecutionContext("__default", request);
        QueryCost cost = handler.canHandle(request, executionContext);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Positive case but the filters are AND'ed in two different child filters.
        Filter filter = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(
                QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
            .addChildFilter(
                QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                    QueryRequestBuilderUtils
                        .createLongLiteralValueExpression(System.currentTimeMillis()))).build();
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
              .addChildFilter(filter)
                .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", 401)))
            .build();

        executionContext = new ExecutionContext("__default", request);

        cost = handler.canHandle(request, executionContext);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Query has only one leaf filter and it matches only one of the view
        // filters
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                    Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL)))
                    .build()))
            .build();

        executionContext = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, executionContext);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Only one view filter is present in the query filters
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                        Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                            Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL)))
                            .build()))
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                        QueryRequestBuilderUtils
                            .createLongLiteralValueExpression(System.currentTimeMillis()))).build())
            .build();

        executionContext = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, executionContext);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case with correct filters but 'OR' operation.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.OR)
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                        Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                            Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL)))
                            .build()))
                .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", 401L)))
            .build();

        executionContext = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, executionContext);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case with a complex filter but 'OR' at the root level, hence shouldn't match.
        filter = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                    Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL))).build()))
            .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", "401"))
            .build();
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.OR)
                .addChildFilter(
                    QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                        Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                            Value.newBuilder().setBoolean(false).setValueType(ValueType.BOOL)))
                            .build()))
                .addChildFilter(filter))
            .build();

        executionContext = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, executionContext);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Value in query filter is different from the value in view filter
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(
                    QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
                .addChildFilter(
                    QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", "200")))
            .build();

        executionContext = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, executionContext);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Unsupported operator in the query filter.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.IN,
                QueryRequestBuilderUtils.createStringLiteralValueExpression("dummy")))
            .build();

        executionContext = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, executionContext);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Any query without filter should not be handled.
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .build();
        executionContext = new ExecutionContext("__default", request);
        QueryCost negativeCost = handler.canHandle(request, executionContext);
        Assertions.assertFalse(negativeCost.getCost() >= 0.0d && negativeCost.getCost() < 1.0d);
      }
    }
  }

  @Test
  public void testCanHandleWithMultipleViewFiltersAndRepeatedQueryFilters() {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"), config.getConfig("requestHandlerInfo"));

      // Verify that the entry span view handler can handle the query which has the filter
      // on the column which has a view filter.
      if (config.getString("name").equals("error-entry-span-view-handler")) {
        // Positive case, straight forward.
        Filter filter = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(
                QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
            .addChildFilter(
                QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", "401"))
            .build();
        QueryRequest request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(filter)
                .addChildFilter(Filter.newBuilder(filter)))
            .build();

        ExecutionContext context = new ExecutionContext("__default", request);
        QueryCost cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Positive case
        Filter childFilter = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(
                QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
            .addChildFilter(
                QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                    QueryRequestBuilderUtils
                        .createLongLiteralValueExpression(System.currentTimeMillis()))).build();
        filter = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(childFilter)
            .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", 401))
            .build();
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(filter).addChildFilter(Filter.newBuilder(filter)))
            .build();

        context = new ExecutionContext("__default", request);

        cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Negative case. Only one view filter is present in the query filters.
        Filter filter1 = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(
                QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                    Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                        Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL)))
                        .build()))
            .addChildFilter(
                QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                    QueryRequestBuilderUtils
                        .createLongLiteralValueExpression(System.currentTimeMillis()))).build();
        Filter filter2 = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(
                QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                    Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                        Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL)))
                        .build()))
            .addChildFilter(
                QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.LT,
                    QueryRequestBuilderUtils
                        .createLongLiteralValueExpression(System.currentTimeMillis() - 10000))).build();
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.AND)
                .addChildFilter(filter1).addChildFilter(filter2))
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertFalse(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);

        // Positive case with a complex filter and 'OR' at the root level, but both sides of 'OR'
        // match the view filter.
        filter1 = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                    Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL))).build()))
            .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", "401"))
            .build();
        filter2 = Filter.newBuilder().setOperator(Operator.AND)
            .addChildFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
                Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                    Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL))).build()))
            .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", "500"))
            .build();
        request = QueryRequest.newBuilder()
            .setDistinctSelections(true)
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
            .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
            .setFilter(Filter.newBuilder().setOperator(Operator.OR)
                .addChildFilter(filter1).addChildFilter(filter2))
            .build();

        context = new ExecutionContext("__default", request);
        cost = handler.canHandle(request, context);
        Assertions.assertTrue(cost.getCost() >= 0.0d && cost.getCost() < 1.0d);
      }
    }
  }

  @Test
  public void testConvertSimpleSelectionsQueryResultSet() throws IOException {
    String[][] resultTable =
        new String[][] {
          {"operation-name-0", "service-name-0", "70", "80"},
          {"operation-name-1", "service-name-1", "71", "79"},
          {"operation-name-2", "service-name-2", "72", "78"},
          {"operation-name-3", "service-name-3", "73", "77"}
        };
    List<String> columnNames = List.of("operation_name", "service_name", "start_time_millis", "duration");
    ResultSet resultSet = mockResultSet(4, 4, columnNames, resultTable);
    ResultSetGroup resultSetGroup = mockResultSetGroup(List.of(resultSet));

    verifyResponseRows(
        pinotBasedRequestHandler.convert(resultSetGroup, new LinkedHashSet<>()), resultTable);
  }

  @Test
  public void testConvertAggregationColumnsQueryResultSet() throws IOException {
    String[][] resultTable =
        new String[][] {
          {"operation-name-10", "110", "40", "21"},
          {"operation-name-11", "111", "41", "22"},
          {"operation-name-12", "112", "42", "23"},
          {"operation-name-13", "113", "43", "24"}
        };
    List<String> columnNames = List.of("operation_name", "avg(duration)", "count(*)", "max(duration)");
    ResultSet resultSet = mockResultSet(4, 4, columnNames, resultTable);
    ResultSetGroup resultSetGroup = mockResultSetGroup(List.of(resultSet));

    verifyResponseRows(
        pinotBasedRequestHandler.convert(resultSetGroup, new LinkedHashSet<>()), resultTable);
  }

  @Test
  public void testConvertSelectionsWithMapKeysAndValuesQueryResultSet() throws IOException {
    String[][] resultTable =
        new String[][] {
          {
            "operation-name-11",
            stringify(List.of("t1", "t2")),
            stringify(List.of("v1", "v2")),
            "service-1",
            stringify(List.of("t10")),
            stringify(List.of("v10"))
          },
          {
            "operation-name-12",
            stringify(List.of("a2")),
            stringify(List.of("b2")),
            "service-2",
            stringify(List.of("c10", "c11")),
            stringify(List.of("d10", "d11"))
          },
          {
            "operation-name-13",
            stringify(List.of()),
            stringify(List.of()),
            "service-3",
            stringify(List.of("e15")),
            stringify(List.of("f15"))
          }
        };
    List<String> columnNames =
        List.of(
            "operation_name",
            "tags1" + ViewDefinition.MAP_KEYS_SUFFIX,
            "tags1" + ViewDefinition.MAP_VALUES_SUFFIX,
            "service_name",
            "tags2" + ViewDefinition.MAP_KEYS_SUFFIX,
            "tags2" + ViewDefinition.MAP_VALUES_SUFFIX);
    ResultSet resultSet = mockResultSet(3, 6, columnNames, resultTable);
    ResultSetGroup resultSetGroup = mockResultSetGroup(List.of(resultSet));

    String[][] expectedRows =
        new String[][] {
          {
            "operation-name-11",
            stringify(Map.of("t1", "v1", "t2", "v2")),
            "service-1",
            stringify(Map.of("t10", "v10"))
          },
          {
            "operation-name-12",
            stringify(Map.of("a2", "b2")),
            "service-2",
            stringify(Map.of("c10", "d10", "c11", "d11"))
          },
          {"operation-name-13", stringify(Map.of()), "service-3", stringify(Map.of("e15", "f15"))}
        };

    verifyResponseRows(
        pinotBasedRequestHandler.convert(resultSetGroup, new LinkedHashSet<>()), expectedRows);
  }

  @Test
  public void testConvertMultipleResultSetsInFResultSetGroup() throws IOException {
    List<String> columnNames = List.of("operation_name", "avg(duration)", "count(*)", "max(duration)");
    String[][] resultTable1 =
        new String[][] {
          {"operation-name-10", "110", "40", "21"},
          {"operation-name-11", "111", "41", "22"},
          {"operation-name-12", "112", "42", "23"},
          {"operation-name-13", "113", "43", "24"}
        };
    ResultSet resultSet1 = mockResultSet(4, 4, columnNames, resultTable1);

    String[][] resultTable2 =
        new String[][] {
          {"operation-name-20", "200", "400", "20000"},
          {"operation-name-22", "220", "420", "22000"}
        };
    ResultSet resultSet2 = mockResultSet(2, 4, columnNames, resultTable2);
    ResultSetGroup resultSetGroup = mockResultSetGroup(List.of(resultSet1, resultSet2));

    String[][] expectedRows =
        new String[][] {
          {"operation-name-10", "110", "40", "21"},
          {"operation-name-11", "111", "41", "22"},
          {"operation-name-12", "112", "42", "23"},
          {"operation-name-13", "113", "43", "24"},
          {"operation-name-20", "200", "400", "20000"},
          {"operation-name-22", "220", "420", "22000"}
        };

    verifyResponseRows(
        pinotBasedRequestHandler.convert(resultSetGroup, new LinkedHashSet<>()), expectedRows);
  }

  @Test
  public void testNullExecutionContextEmitsNPE() {
    Assertions.assertThrows(
        NullPointerException.class,
        () ->
            pinotBasedRequestHandler
                .handleRequest(QueryRequest.newBuilder().build(), mock(ExecutionContext.class))
                .blockingSubscribe());
  }

  @Test
  public void testNullTenantIdQueryRequestContextThrowsNPE() {
    Assertions.assertThrows(
        NullPointerException.class,
        () ->
            pinotBasedRequestHandler
                .handleRequest(QueryRequest.newBuilder().build(), mock(ExecutionContext.class))
                .blockingSubscribe());
  }

  @Test
  public void
      testGroupBysAndAggregationsMixedWithSelectionsThrowsExceptionWhenDistinctSelectionIsSpecified() {
    // Setting distinct selections and mixing selections and group bys should throw exception
    QueryRequest request = QueryRequest.newBuilder()
        .setDistinctSelections(true)
        .addSelection(QueryRequestBuilderUtils.createColumnExpression("col1"))
        .addSelection(QueryRequestBuilderUtils.createColumnExpression("col2"))
        .addGroupBy(QueryRequestBuilderUtils.createColumnExpression("col3"))
        .build();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> pinotBasedRequestHandler.handleRequest(
            request,
            new ExecutionContext("test-tenant-id", request)).blockingSubscribe());

    // Setting distinct selections and mixing selections and aggregations should throw exception
    QueryRequest request2 = QueryRequest.newBuilder()
        .setDistinctSelections(true)
        .addSelection(QueryRequestBuilderUtils.createColumnExpression("col1"))
        .addSelection(QueryRequestBuilderUtils.createColumnExpression("col2"))
        .addAggregation(
            QueryRequestBuilderUtils.createFunctionExpression(
                "AVG", "duration", "avg_duration"))
        .build();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            pinotBasedRequestHandler
                .handleRequest(request2, new ExecutionContext("test-tenant-id", request2))
                .blockingSubscribe());

    // Setting distinct selections and mixing selections, group bys and aggregations should throw
    // exception
    QueryRequest request3 = QueryRequest.newBuilder()
        .setDistinctSelections(true)
        .addSelection(QueryRequestBuilderUtils.createColumnExpression("col1"))
        .addSelection(QueryRequestBuilderUtils.createColumnExpression("col2"))
        .addGroupBy(QueryRequestBuilderUtils.createColumnExpression("col3"))
        .addAggregation(
            QueryRequestBuilderUtils.createFunctionExpression(
                "AVG", "duration", "avg_duration"))
        .build();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            pinotBasedRequestHandler
                .handleRequest(request3, new ExecutionContext("test-tenant-id", request3))
                .blockingSubscribe());
  }

  @Test
  public void testWithMockPinotClient() throws IOException {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      if (!config.getString("name").equals("trace-view-handler")) {
        continue;
      }

      // Mock the PinotClient
      PinotClient pinotClient = mock(PinotClient.class);
      PinotClientFactory factory = mock(PinotClientFactory.class);
      when(factory.getPinotClient(any())).thenReturn(pinotClient);

      String[][] resultTable =
          new String[][] {
              {"trace-id-1", "80"},
              {"trace-id-2", "79"},
              {"trace-id-3", "78"},
              {"trace-id-4", "77"}
          };
      List<String> columnNames = List.of("trace_id", "duration_millis");
      ResultSet resultSet = mockResultSet(4, 2, columnNames, resultTable);
      ResultSetGroup resultSetGroup = mockResultSetGroup(List.of(resultSet));
      when(pinotClient.executeQuery(any(), any())).thenReturn(resultSetGroup);

      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"),
              config.getConfig("requestHandlerInfo"),
              new ResultSetTypePredicateProvider() {
                @Override
                public boolean isSelectionResultSetType(ResultSet resultSet) {
                  return true;
                }

                @Override
                public boolean isResultTableResultSetType(ResultSet resultSet) {
                  return false;
                }
              },
              factory);

      QueryRequest request = QueryRequest.newBuilder()
          .addSelection(QueryRequestBuilderUtils.createColumnExpression("Trace.id"))
          .addSelection(QueryRequestBuilderUtils.createColumnExpression("Trace.duration_millis"))
          .build();
      ExecutionContext context = new ExecutionContext("__default", request);
      verifyResponseRows(handler.handleRequest(request, context), resultTable);
    }
  }

  @Test
  public void testViewColumnFilterRemoval() throws IOException {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      if (!config.getString("name").equals("span-event-view-handler")) {
        continue;
      }

      // Mock the PinotClient
      PinotClient pinotClient = mock(PinotClient.class);
      PinotClientFactory factory = mock(PinotClientFactory.class);
      when(factory.getPinotClient(any())).thenReturn(pinotClient);

      String[][] resultTable =
          new String[][] {
              {"test-span-id-1", "trace-id-1"},
              {"test-span-id-2", "trace-id-1"},
              {"test-span-id-3", "trace-id-1"},
              {"test-span-id-4", "trace-id-2"}
          };
      List<String> columnNames = List.of("span_id", "trace_id");
      ResultSet resultSet = mockResultSet(4, 2, columnNames, resultTable);
      ResultSetGroup resultSetGroup = mockResultSetGroup(List.of(resultSet));

      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"),
              config.getConfig("requestHandlerInfo"),
              new ResultSetTypePredicateProvider() {
                @Override
                public boolean isSelectionResultSetType(ResultSet resultSet) {
                  return true;
                }

                @Override
                public boolean isResultTableResultSetType(ResultSet resultSet) {
                  return false;
                }
              },
              factory);

      QueryRequest request = QueryRequest.newBuilder()
          .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
          .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
          .setFilter(Filter.newBuilder().setOperator(Operator.AND)
              .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.isEntrySpan", "true"))
              .addChildFilter(QueryRequestBuilderUtils.createFilter("EVENT.startTime", Operator.GT,
                  QueryRequestBuilderUtils.createStringLiteralValueExpression("1000")))
              .addChildFilter(QueryRequestBuilderUtils.createInFilter("EVENT.isEntrySpan", List.of("true", "false"))))
          .build();
      ExecutionContext context = new ExecutionContext("__default", request);

      QueryCost cost = handler.canHandle(request, context);
      Assertions.assertTrue(cost.getCost() > 0.0d && cost.getCost() < 1d);

      // The query filter is based on both isEntrySpan and startTime. Since the viewFilter
      // checks for both the true and false values of isEntrySpan and query filter only needs
      // "true", isEntrySpan predicate is still passed to the store in the query.
      String expectedQuery = "Select span_id, trace_id FROM spanEventView WHERE tenant_id = ? AND ( is_entry_span = ? AND start_time_millis > ? )";
      Params params = Params.newBuilder().addStringParam("__default").addStringParam("true").addStringParam("1000").build();
      when(pinotClient.executeQuery(expectedQuery, params)).thenReturn(resultSetGroup);

      verifyResponseRows(handler.handleRequest(request, context), resultTable);
    }
  }

  @Test
  public void testViewColumnFilterRemovalComplexCase() throws IOException {
    for (Config config : serviceConfig.getConfigList("queryRequestHandlersConfig")) {
      if (!config.getString("name").equals("error-entry-span-view-handler")) {
        continue;
      }

      // Mock the PinotClient
      PinotClient pinotClient = mock(PinotClient.class);
      PinotClientFactory factory = mock(PinotClientFactory.class);
      when(factory.getPinotClient(any())).thenReturn(pinotClient);

      String[][] resultTable =
          new String[][] {
              {"1598922083000", "span-1", "trace-1"},
              {"1598922083010", "span-2", "trace-1"},
              {"1598922083020", "span-3", "trace-1"},
              {"1598922083230", "span-4", "trace-2"}
          };
      List<String> columnNames = List.of("start_time_millis", "span_id", "trace_id");
      ResultSet resultSet = mockResultSet(4, 3, columnNames, resultTable);
      ResultSetGroup resultSetGroup = mockResultSetGroup(List.of(resultSet));

      PinotBasedRequestHandler handler =
          new PinotBasedRequestHandler(
              config.getString("name"),
              config.getConfig("requestHandlerInfo"),
              new ResultSetTypePredicateProvider() {
                @Override
                public boolean isSelectionResultSetType(ResultSet resultSet) {
                  return true;
                }

                @Override
                public boolean isResultTableResultSetType(ResultSet resultSet) {
                  return false;
                }
              },
              factory);

      Filter filter1 = Filter.newBuilder().setOperator(Operator.AND)
          .addChildFilter(QueryRequestBuilderUtils.createFilter("EVENT.isEntrySpan", Operator.EQ,
              Expression.newBuilder().setLiteral(LiteralConstant.newBuilder().setValue(
                  Value.newBuilder().setBoolean(true).setValueType(ValueType.BOOL))).build()))
          .addChildFilter(QueryRequestBuilderUtils.createEqualsFilter("EVENT.statusCode", "401"))
          .build();
      QueryRequest request = QueryRequest.newBuilder()
          .setDistinctSelections(true)
          .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.startTime"))
          .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.id"))
          .addSelection(QueryRequestBuilderUtils.createColumnExpression("EVENT.traceId"))
          .setFilter(Filter.newBuilder().setOperator(Operator.OR)
              .addChildFilter(filter1).addChildFilter(Filter.newBuilder(filter1)))
          .build();
      ExecutionContext context = new ExecutionContext("__default", request);
      QueryCost cost = handler.canHandle(request, context);
      Assertions.assertTrue(cost.getCost() > 0.0d && cost.getCost() < 1d);

      // Though there is isEntrySpan and statusCode used in the filters, they both should be
      // removed by the view filters and hence the actual query only has tenant_id in the filter.
      String expectedQuery = "Select DISTINCT start_time_millis, span_id, trace_id FROM spanEventView WHERE tenant_id = ? AND status_code = ?";
      Params params = Params.newBuilder().addStringParam("__default").addStringParam("401").build();
      when(pinotClient.executeQuery(expectedQuery, params)).thenReturn(resultSetGroup);

      verifyResponseRows(handler.handleRequest(request, context), resultTable);
    }
  }

  private ResultSet mockResultSet(
      int rowCount, int columnCount, List<String> columnNames, String[][] resultsTable) {
    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getRowCount()).thenReturn(rowCount);
    when(resultSet.getColumnCount()).thenReturn(columnCount);
    for (int colIdx = 0; colIdx < columnNames.size(); colIdx++) {
      when(resultSet.getColumnName(colIdx)).thenReturn(columnNames.get(colIdx));
    }

    for (int rowIdx = 0; rowIdx < resultsTable.length; rowIdx++) {
      for (int colIdx = 0; colIdx < resultsTable[0].length; colIdx++) {
        when(resultSet.getString(rowIdx, colIdx)).thenReturn(resultsTable[rowIdx][colIdx]);
      }
    }

    return resultSet;
  }

  private ResultSetGroup mockResultSetGroup(List<ResultSet> resultSets) {
    ResultSetGroup resultSetGroup = mock(ResultSetGroup.class);

    when(resultSetGroup.getResultSetCount()).thenReturn(resultSets.size());
    for (int i = 0; i < resultSets.size(); i++) {
      when(resultSetGroup.getResultSet(i)).thenReturn(resultSets.get(i));
    }

    return resultSetGroup;
  }

  private void verifyResponseRows(Observable<Row> rowObservable, String[][] expectedResultTable)
      throws IOException {
    List<Row> rows = rowObservable.toList().blockingGet();
    Assertions.assertEquals(expectedResultTable.length, rows.size());
    for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
      Row row = rows.get(rowIdx);
      Assertions.assertEquals(expectedResultTable[rowIdx].length, row.getColumnCount());
      for (int colIdx = 0; colIdx < row.getColumnCount(); colIdx++) {
        String val = row.getColumn(colIdx).getString();
        // In the scope of our unit tests, this is a map. Cannot JSON object comparison on it since
        // it's not ordered.
        if (val.startsWith("{") && val.endsWith("}")) {
          Assertions.assertEquals(
              objectMapper.readTree(expectedResultTable[rowIdx][colIdx]),
              objectMapper.readTree(val));
        } else {
          Assertions.assertEquals(expectedResultTable[rowIdx][colIdx], val);
        }
      }
    }
  }

  private String stringify(Object obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }
}
