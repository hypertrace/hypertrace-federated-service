package org.hypertrace.core.query.service.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hypertrace.core.query.service.api.ColumnIdentifier;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.Function;
import org.hypertrace.core.query.service.api.LiteralConstant;
import org.hypertrace.core.query.service.api.Operator;
import org.hypertrace.core.query.service.api.OrderByExpression;
import org.hypertrace.core.query.service.api.SortOrder;
import org.hypertrace.core.query.service.api.Value;
import org.hypertrace.core.query.service.api.ValueType;

/**
 * Utility methods to easily create {@link org.hypertrace.core.query.service.api.QueryRequest} its
 * selections and filters.
 */
@Deprecated
public class QueryRequestUtil {

  public static Filter createTimeFilter(String columnName, Operator op, long value) {
    ColumnIdentifier.Builder timeColumn = ColumnIdentifier.newBuilder().setColumnName(columnName);
    Expression.Builder lhs = Expression.newBuilder().setColumnIdentifier(timeColumn);

    LiteralConstant.Builder constant =
        LiteralConstant.newBuilder()
            .setValue(
                Value.newBuilder().setValueType(ValueType.STRING).setString(String.valueOf(value)));
    Expression.Builder rhs = Expression.newBuilder().setLiteral(constant);
    return Filter.newBuilder().setLhs(lhs).setOperator(op).setRhs(rhs).build();
  }

  public static Filter.Builder createBetweenTimesFilter(
      String columnName, long lower, long higher) {
    return Filter.newBuilder()
        .setOperator(Operator.AND)
        .addChildFilter(createTimeFilter(columnName, Operator.GE, lower))
        .addChildFilter(createTimeFilter(columnName, Operator.LT, higher));
  }

  /** Given a column name, creates and returns an expression to select count(columnName). */
  public static Expression.Builder createCountByColumnSelection(String... columnNames) {
    Function.Builder count =
        Function.newBuilder()
            .setFunctionName("Count")
            .addAllArguments(
                Arrays.stream(columnNames)
                    .map(
                        columnName ->
                            Expression.newBuilder()
                                .setColumnIdentifier(
                                    ColumnIdentifier.newBuilder().setColumnName(columnName))
                                .build())
                    .collect(Collectors.toList()));
    return Expression.newBuilder().setFunction(count);
  }

  public static Filter.Builder createColumnValueFilter(
      String columnName, Operator operator, String value) {
    ColumnIdentifier.Builder column = ColumnIdentifier.newBuilder().setColumnName(columnName);
    Expression.Builder lhs = Expression.newBuilder().setColumnIdentifier(column);

    LiteralConstant.Builder constant =
        LiteralConstant.newBuilder()
            .setValue(Value.newBuilder().setValueType(ValueType.STRING).setString(value));
    Expression.Builder rhs = Expression.newBuilder().setLiteral(constant);
    return Filter.newBuilder().setLhs(lhs).setOperator(operator).setRhs(rhs);
  }

  public static Filter.Builder createBooleanFilter(Operator operator, List<Filter> childFilters) {
    return Filter.newBuilder().setOperator(operator).addAllChildFilter(childFilters);
  }

  public static Filter.Builder createValueInFilter(String columnName, Collection<String> values) {
    return createValuesOpFilter(columnName, values, Operator.IN);
  }

  public static Expression.Builder createColumnExpression(String columnName) {
    return Expression.newBuilder()
        .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName(columnName));
  }

  public static Expression.Builder createFunctionExpression(
      String functionName, Expression... expressions) {
    Function.Builder functionBuilder = Function.newBuilder().setFunctionName(functionName);
    for (Expression e : expressions) {
      functionBuilder.addArguments(e);
    }

    return Expression.newBuilder().setFunction(functionBuilder);
  }

  public static Expression.Builder createStringLiteralExpression(String value) {
    LiteralConstant.Builder constant =
        LiteralConstant.newBuilder()
            .setValue(
                Value.newBuilder().setValueType(ValueType.STRING).setString(String.valueOf(value)));
    Expression.Builder expression = Expression.newBuilder().setLiteral(constant);

    return expression;
  }

  public static Expression.Builder createLongLiteralExpression(Long value) {
    LiteralConstant.Builder constant =
        LiteralConstant.newBuilder()
            .setValue(Value.newBuilder().setValueType(ValueType.LONG).setLong(value));
    Expression.Builder expression = Expression.newBuilder().setLiteral(constant);

    return expression;
  }

  public static OrderByExpression.Builder createOrderByExpression(
      String columnName, SortOrder order) {
    return OrderByExpression.newBuilder()
        .setOrder(order)
        .setExpression(createColumnExpression(columnName));
  }

  public static Function.Builder createTimeColumnGroupByFunction(
      String timeColumn, long periodSecs) {
    return Function.newBuilder()
        .setFunctionName("dateTimeConvert")
        .addArguments(QueryRequestUtil.createColumnExpression(timeColumn))
        .addArguments(
            Expression.newBuilder()
                .setLiteral(
                    LiteralConstant.newBuilder()
                        .setValue(Value.newBuilder().setString("1:MILLISECONDS:EPOCH"))))
        .addArguments(
            Expression.newBuilder()
                .setLiteral(
                    LiteralConstant.newBuilder()
                        .setValue(Value.newBuilder().setString("1:MILLISECONDS:EPOCH"))))
        .addArguments(
            Expression.newBuilder()
                .setLiteral(
                    LiteralConstant.newBuilder()
                        .setValue(Value.newBuilder().setString(periodSecs + ":SECONDS"))));
  }

  public static Filter createValueEQFilter(List<String> idColumns, List<String> idColumnsValues) {
    if (idColumns.size() != idColumnsValues.size()) {
      throw new IllegalArgumentException(
          String.format(
              "Literal for composite id column doesn't have required number of values."
                  + " Invalid idColumnsValues:%s for idColumns:%s",
              idColumnsValues, idColumns));
    }
    List<Filter> childFilters =
        IntStream.range(0, idColumnsValues.size())
            .mapToObj(
                i ->
                    Filter.newBuilder()
                        .setLhs(createColumnExpression(idColumns.get(i)))
                        .setOperator(Operator.EQ)
                        .setRhs(getLiteralExpression(idColumnsValues.get(i)))
                        .build())
            .collect(Collectors.toList());
    return Filter.newBuilder().setOperator(Operator.AND).addAllChildFilter(childFilters).build();
  }

  private static Expression.Builder getLiteralExpression(String value) {
    return Expression.newBuilder()
        .setLiteral(
            LiteralConstant.newBuilder()
                .setValue(Value.newBuilder().setString(value).setValueType(ValueType.STRING)));
  }

  private static Filter.Builder createValuesOpFilter(
      String columnName, Collection<String> values, Operator op) {
    ColumnIdentifier.Builder column = ColumnIdentifier.newBuilder().setColumnName(columnName);
    Expression.Builder lhs = Expression.newBuilder().setColumnIdentifier(column);

    LiteralConstant.Builder constant =
        LiteralConstant.newBuilder()
            .setValue(
                Value.newBuilder().setValueType(ValueType.STRING_ARRAY).addAllStringArray(values));
    Expression.Builder rhs = Expression.newBuilder().setLiteral(constant);
    return Filter.newBuilder().setLhs(lhs).setOperator(op).setRhs(rhs);
  }
}
