package org.hypertrace.core.query.service.util;

import org.hypertrace.core.query.service.api.ColumnIdentifier;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.LiteralConstant;
import org.hypertrace.core.query.service.api.Operator;
import org.hypertrace.core.query.service.api.Value;
import org.hypertrace.core.query.service.api.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryRequestUtilTest {
  @Test
  public void testCreateBetweenTimesFilter() {
    Filter.Builder timeFilter =
        QueryRequestUtil.createBetweenTimesFilter("API.startTime", 20L, 30L);
    Assertions.assertEquals(
        Filter.newBuilder()
            .setOperator(Operator.AND)
            .addChildFilter(
                Filter.newBuilder()
                    .setLhs(
                        Expression.newBuilder()
                            .setColumnIdentifier(
                                ColumnIdentifier.newBuilder().setColumnName("API.startTime")))
                    .setOperator(Operator.GE)
                    .setRhs(
                        Expression.newBuilder()
                            .setLiteral(
                                LiteralConstant.newBuilder()
                                    .setValue(
                                        Value.newBuilder()
                                            .setValueType(ValueType.STRING)
                                            .setString("20")))))
            .addChildFilter(
                Filter.newBuilder()
                    .setLhs(
                        Expression.newBuilder()
                            .setColumnIdentifier(
                                ColumnIdentifier.newBuilder().setColumnName("API.startTime")))
                    .setOperator(Operator.LT)
                    .setRhs(
                        Expression.newBuilder()
                            .setLiteral(
                                LiteralConstant.newBuilder()
                                    .setValue(
                                        Value.newBuilder()
                                            .setValueType(ValueType.STRING)
                                            .setString("30")))))
            .build(),
        timeFilter.build());
  }
}
