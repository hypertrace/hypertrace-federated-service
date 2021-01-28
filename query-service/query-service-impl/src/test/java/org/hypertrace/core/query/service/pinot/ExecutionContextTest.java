package org.hypertrace.core.query.service.pinot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Set;
import org.hypertrace.core.query.service.ExecutionContext;
import org.hypertrace.core.query.service.QueryRequestBuilderUtils;
import org.hypertrace.core.query.service.api.ColumnIdentifier;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.Function;
import org.hypertrace.core.query.service.api.LiteralConstant;
import org.hypertrace.core.query.service.api.Operator;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.QueryRequest.Builder;
import org.hypertrace.core.query.service.api.ResultSetMetadata;
import org.hypertrace.core.query.service.api.Value;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionContextTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContextTest.class);

  @Test
  public void testRepeatedColumns() {
    Builder builder = QueryRequest.newBuilder();
    // agg function with alias
    Function count =
        Function.newBuilder()
            .setFunctionName("Count")
            .setAlias("myCountAlias")
            .addArguments(
                Expression.newBuilder()
                    .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.id")))
            .build();
    builder.addAggregation(Expression.newBuilder().setFunction(count));

    // agg function without alias
    Function minFunction =
        Function.newBuilder()
            .setFunctionName("MIN")
            .addArguments(
                Expression.newBuilder()
                    .setColumnIdentifier(
                        ColumnIdentifier.newBuilder().setColumnName("Trace.duration")))
            .build();
    builder.addAggregation(Expression.newBuilder().setFunction(minFunction));

    builder.addSelection(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name")));

    builder.addSelection(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name")));

    builder.addGroupBy(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name")));
    QueryRequest queryRequest = builder.build();

    ExecutionContext context = new ExecutionContext("test", queryRequest);
    ResultSetMetadata resultSetMetadata = context.getResultSetMetadata();
    System.out.println("resultSetMetadata = " + resultSetMetadata);

    assertNotNull(resultSetMetadata);
    assertEquals(3, resultSetMetadata.getColumnMetadataCount());
    assertEquals("Trace.transaction_name", resultSetMetadata.getColumnMetadata(0).getColumnName());
    assertEquals("myCountAlias", resultSetMetadata.getColumnMetadata(1).getColumnName());
    assertEquals("MIN", resultSetMetadata.getColumnMetadata(2).getColumnName());

    // Selections should correspond in size and order to the
    // resultSetMetadata.getColumnMetadataList()
    assertEquals(3, context.getAllSelections().size());
    Iterator<Expression> selectionsIterator = context.getAllSelections().iterator();
    assertEquals(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name"))
            .build(),
        selectionsIterator.next());
    assertEquals(Expression.newBuilder().setFunction(count).build(), selectionsIterator.next());
    assertEquals(
        Expression.newBuilder().setFunction(minFunction).build(), selectionsIterator.next());
  }

  @Test
  public void testFiltersWithLiterals() {
    Builder builder = QueryRequest.newBuilder();
    builder.addSelection(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name")));
    Expression expression =
        Expression.newBuilder()
            .setLiteral(LiteralConstant.newBuilder().setValue(Value.newBuilder().setString("test")))
            .build();
    builder.setFilter(
        Filter.newBuilder()
            .setLhs(
                Expression.newBuilder()
                    .setColumnIdentifier(
                        ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name")))
            .setRhs(expression)
            .setOperator(Operator.EQ));

    QueryRequest queryRequest = builder.build();

    ExecutionContext context = new ExecutionContext("test", queryRequest);
    ResultSetMetadata resultSetMetadata = context.getResultSetMetadata();
    LOGGER.info("resultSetMetadata = " + resultSetMetadata);

    assertNotNull(resultSetMetadata);
    assertEquals(1, resultSetMetadata.getColumnMetadataCount());
    assertEquals("Trace.transaction_name", resultSetMetadata.getColumnMetadata(0).getColumnName());

    // Selections should correspond in size and order to the
    // resultSetMetadata.getColumnMetadataList()
    assertEquals(1, context.getAllSelections().size());
    Iterator<Expression> selectionsIterator = context.getAllSelections().iterator();
    assertEquals(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name"))
            .build(),
        selectionsIterator.next());
  }

  @Test
  public void testReferencedColumns() {
    Builder builder = QueryRequest.newBuilder();
    builder.addSelection(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name")));
    Expression expression =
        Expression.newBuilder()
            .setLiteral(LiteralConstant.newBuilder().setValue(Value.newBuilder().setString("test")))
            .build();
    Filter.Builder idFilter =
        Filter.newBuilder()
            .setLhs(
                Expression.newBuilder()
                    .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.id")))
            .setRhs(expression)
            .setOperator(Operator.EQ);
    Filter startTimeFilter =
        QueryRequestBuilderUtils.createTimeFilter(
            "Trace.start_time_millis",
            Operator.GT,
            System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Filter endTimeFilter =
        QueryRequestBuilderUtils.createTimeFilter(
            "Trace.end_time_millis", Operator.LT, System.currentTimeMillis());

    Filter andFilter =
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(startTimeFilter)
            .addChildFilter(endTimeFilter)
            .addChildFilter(idFilter)
            .build();
    builder.setFilter(andFilter);

    QueryRequest queryRequest = builder.build();

    ExecutionContext context = new ExecutionContext("test", queryRequest);

    Set<String> selectedColumns = context.getSelectedColumns();
    assertNotNull(selectedColumns);
    assertEquals(1, selectedColumns.size());
    assertEquals("Trace.transaction_name", selectedColumns.iterator().next());

    Set<String> referencedColumns = context.getReferencedColumns();
    assertNotNull(referencedColumns);
    assertEquals(4, referencedColumns.size());
    assertEquals(
        ImmutableSet.of(
            "Trace.transaction_name",
            "Trace.id",
            "Trace.start_time_millis",
            "Trace.end_time_millis"),
        referencedColumns);

    ResultSetMetadata resultSetMetadata = context.getResultSetMetadata();
    assertNotNull(resultSetMetadata);
    assertEquals(1, resultSetMetadata.getColumnMetadataCount());
    assertEquals("Trace.transaction_name", resultSetMetadata.getColumnMetadata(0).getColumnName());

    // Selections should correspond in size and order to the
    // resultSetMetadata.getColumnMetadataList()
    assertEquals(1, context.getAllSelections().size());
    Iterator<Expression> selectionsIterator = context.getAllSelections().iterator();
    assertEquals(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name"))
            .build(),
        selectionsIterator.next());
  }

  @Test
  public void testSelectionsLinkedHashSet() {
    Builder builder = QueryRequest.newBuilder();
    // agg function with alias
    Function count =
        Function.newBuilder()
            .setFunctionName("Count")
            .setAlias("myCountAlias")
            .addArguments(
                Expression.newBuilder()
                    .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.id")))
            .build();
    builder.addAggregation(Expression.newBuilder().setFunction(count));

    // agg function without alias
    Function minFunction =
        Function.newBuilder()
            .setFunctionName("MIN")
            .addArguments(
                Expression.newBuilder()
                    .setColumnIdentifier(
                        ColumnIdentifier.newBuilder().setColumnName("Trace.duration")))
            .build();
    builder.addAggregation(Expression.newBuilder().setFunction(minFunction));

    // Add some selections
    builder.addSelection(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name")));
    builder.addSelection(
        Expression.newBuilder()
            .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.id")));

    // An function added into selections list is treated as a selection
    Function avg =
        Function.newBuilder()
            .setFunctionName("AVG")
            .setAlias("myAvgAlias")
            .addArguments(
                Expression.newBuilder()
                    .setColumnIdentifier(
                        ColumnIdentifier.newBuilder().setColumnName("Trace.duration")))
            .build();
    builder.addSelection(Expression.newBuilder().setFunction(avg));

    // Add some group bys
    builder.addGroupBy(
        Expression.newBuilder()
            .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.api_name")));
    builder.addGroupBy(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.service_name")));
    QueryRequest queryRequest = builder.build();

    ExecutionContext context = new ExecutionContext("test", queryRequest);

    // The order in resultSetMetadata.getColumnMetadataList() and selections is group bys,
    // selections then aggregations
    ResultSetMetadata resultSetMetadata = context.getResultSetMetadata();

    assertNotNull(resultSetMetadata);
    assertEquals(7, resultSetMetadata.getColumnMetadataCount());
    assertEquals("Trace.api_name", resultSetMetadata.getColumnMetadata(0).getColumnName());
    assertEquals("Trace.service_name", resultSetMetadata.getColumnMetadata(1).getColumnName());
    assertEquals("Trace.transaction_name", resultSetMetadata.getColumnMetadata(2).getColumnName());
    assertEquals("Trace.id", resultSetMetadata.getColumnMetadata(3).getColumnName());
    assertEquals("myAvgAlias", resultSetMetadata.getColumnMetadata(4).getColumnName());
    assertEquals("myCountAlias", resultSetMetadata.getColumnMetadata(5).getColumnName());
    assertEquals("MIN", resultSetMetadata.getColumnMetadata(6).getColumnName());

    // Selections should correspond in size and order to the
    // resultSetMetadata.getColumnMetadataList()
    assertEquals(7, context.getAllSelections().size());
    Iterator<Expression> selectionsIterator = context.getAllSelections().iterator();
    assertEquals(
        Expression.newBuilder()
            .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.api_name"))
            .build(),
        selectionsIterator.next());
    assertEquals(
        Expression.newBuilder()
            .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.service_name"))
            .build(),
        selectionsIterator.next());
    assertEquals(
        Expression.newBuilder()
            .setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("Trace.transaction_name"))
            .build(),
        selectionsIterator.next());
    assertEquals(
        Expression.newBuilder()
            .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName("Trace.id"))
            .build(),
        selectionsIterator.next());
    assertEquals(Expression.newBuilder().setFunction(avg).build(), selectionsIterator.next());
    assertEquals(Expression.newBuilder().setFunction(count).build(), selectionsIterator.next());
    assertEquals(
        Expression.newBuilder().setFunction(minFunction).build(), selectionsIterator.next());
  }
}
