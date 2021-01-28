package org.hypertrace.core.query.service.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link QueryRequest}
 */
public class QueryRequestTest {
  @Test
  public void testJsonSerializeDeserialize() throws InvalidProtocolBufferException {
    QueryRequest.Builder builder = QueryRequest.newBuilder()
        .addSelection(Expression.newBuilder().setColumnIdentifier(
            ColumnIdentifier.newBuilder().setColumnName("test").setAlias("my_column").build()))
        .addAggregation(Expression.newBuilder().setFunction(
            Function.newBuilder().setFunctionName("SUM").build()))
        .addGroupBy(Expression.newBuilder().setColumnIdentifier(
            ColumnIdentifier.newBuilder().setColumnName("test").build()))
        .addOrderBy(OrderByExpression.newBuilder().setOrderValue(-1).setExpression(
            Expression.newBuilder().setColumnIdentifier(
                ColumnIdentifier.newBuilder().setColumnName("column2"))))
        .setOffset(0).setLimit(1);

    QueryRequest request = builder.build();
    // Serialize into json.
    String json = JsonFormat.printer().omittingInsignificantWhitespace().print(request);
    System.out.println(json);

    // Deserialize and assert.
    QueryRequest.Builder newBuilder = QueryRequest.newBuilder();
    JsonFormat.parser().merge(json, newBuilder);
    Assertions.assertEquals(request, newBuilder.build());
  }
}
