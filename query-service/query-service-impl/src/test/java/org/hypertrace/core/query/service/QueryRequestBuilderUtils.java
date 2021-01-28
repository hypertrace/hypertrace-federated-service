package org.hypertrace.core.query.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

public class QueryRequestBuilderUtils {
  public static Expression.Builder createColumnExpression(String columnName) {
    return Expression.newBuilder()
        .setColumnIdentifier(ColumnIdentifier.newBuilder().setColumnName(columnName));
  }

  public static Expression createAliasedColumnExpression(String columnName, String alias) {
    return Expression.newBuilder()
        .setColumnIdentifier(
            ColumnIdentifier.newBuilder().setColumnName(columnName).setAlias(alias))
        .build();
  }

  public static Expression.Builder createFunctionExpression(
      String functionName, String columnNameArg, String alias) {
    return Expression.newBuilder()
        .setFunction(
            Function.newBuilder()
                .setAlias(alias)
                .setFunctionName(functionName)
                .addArguments(
                    Expression.newBuilder()
                        .setColumnIdentifier(
                            ColumnIdentifier.newBuilder().setColumnName(columnNameArg))));
  }

  public static Expression createFunctionExpression(
      String functionName, String alias, Expression... expressions) {
    return Expression.newBuilder()
        .setFunction(
            Function.newBuilder()
                .setFunctionName(functionName)
                .setAlias(alias)
                .addAllArguments(Arrays.asList(expressions)))
        .build();
  }

  public static Expression createFunctionExpression(
      String functionName, Expression... expressions) {
    return Expression.newBuilder()
        .setFunction(
            Function.newBuilder()
                .setFunctionName(functionName)
                .addAllArguments(Arrays.asList(expressions)))
        .build();
  }

  public static OrderByExpression.Builder createOrderByExpression(
      Expression.Builder expression, SortOrder sortOrder) {
    return OrderByExpression.newBuilder().setExpression(expression).setOrder(sortOrder);
  }

  public static Filter createEqualsFilter(String column, String value) {
    return createFilter(column, Operator.EQ, createStringLiteralValueExpression(value));
  }

  public static Filter createInFilter(String column, List<String> values) {
    return createFilter(column, Operator.IN, createStringArrayLiteralValueExpression(values));
  }

  public static Filter createEqualsFilter(String column, long value) {
    return createFilter(column, Operator.EQ, createLongLiteralValueExpression(value));
  }

  public static Filter createEqualsFilter(String column, int value) {
    return createFilter(column, Operator.EQ, createIntLiteralValueExpression(value));
  }

  public static Filter createFilter(String column, Operator operator, Expression expression) {
    return createFilter(createColumnExpression(column).build(), operator, expression);
  }

  public static Filter createFilter(
      Expression columnExpression, Operator operator, Expression expression) {
    return Filter.newBuilder()
        .setLhs(columnExpression)
        .setOperator(operator)
        .setRhs(expression)
        .build();
  }

  public static Expression createStringLiteralValueExpression(String value) {
    return Expression.newBuilder()
        .setLiteral(
            LiteralConstant.newBuilder()
                .setValue(Value.newBuilder().setString(value).setValueType(ValueType.STRING)))
        .build();
  }

  public static Expression createStringArrayLiteralValueExpression(List<String> values) {
    return Expression.newBuilder()
        .setLiteral(
            LiteralConstant.newBuilder()
                .setValue(
                    Value.newBuilder()
                        .addAllStringArray(values)
                        .setValueType(ValueType.STRING_ARRAY)))
        .build();
  }

  public static Expression createLongLiteralValueExpression(long value) {
    return Expression.newBuilder()
        .setLiteral(
            LiteralConstant.newBuilder()
                .setValue(Value.newBuilder().setLong(value).setValueType(ValueType.LONG)))
        .build();
  }

  public static Expression createIntLiteralValueExpression(int value) {
    return Expression.newBuilder()
        .setLiteral(
            LiteralConstant.newBuilder()
                .setValue(Value.newBuilder().setInt(value).setValueType(ValueType.INT)))
        .build();
  }

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

  public static Filter.Builder createCompositeFilter(Operator operator, Filter... childFilters) {
    return Filter.newBuilder().setOperator(operator).addAllChildFilter(Arrays.asList(childFilters));
  }

  public static OrderByExpression.Builder createOrderByExpression(
      String columnName, SortOrder order) {
    return OrderByExpression.newBuilder()
        .setOrder(order)
        .setExpression(createColumnExpression(columnName));
  }
}
