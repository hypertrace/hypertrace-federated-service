package org.hypertrace.core.query.service.pinot.converters;

import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_AVG;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_CONCAT;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_COUNT;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_DISTINCTCOUNT;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_MAX;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_MIN;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_PERCENTILE;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_SUM;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createColumnExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createIntLiteralValueExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createLongLiteralValueExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createStringLiteralValueExpression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Expression.Builder;
import org.hypertrace.core.query.service.api.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PinotFunctionConverterTest {

  @Mock java.util.function.Function<Expression, String> mockArgumentConverter;

  @Test
  void convertsCountStar() {
    String expected = "COUNT(*)";
    Function countFunction = buildFunction(QUERY_FUNCTION_COUNT, createColumnExpression("foo"));

    assertEquals(
        expected, new PinotFunctionConverter().convert(countFunction, this.mockArgumentConverter));

    // Should not be case sensitive
    Function lowerCaseCountFunction = buildFunction("count", createColumnExpression("foo"));
    assertEquals(
        expected,
        new PinotFunctionConverter().convert(lowerCaseCountFunction, this.mockArgumentConverter));
  }

  @Test
  void convertsPercentileWithValueArg() {
    String expected = "PERCENTILETDIGEST90(fooOut)";
    Expression columnExpression = createColumnExpression("fooIn").build();
    Function intPercentileFunction =
        buildFunction(
            QUERY_FUNCTION_PERCENTILE,
            createIntLiteralValueExpression(90).toBuilder(),
            columnExpression.toBuilder());
    when(this.mockArgumentConverter.apply(columnExpression)).thenReturn("fooOut");

    assertEquals(
        expected,
        new PinotFunctionConverter().convert(intPercentileFunction, this.mockArgumentConverter));

    // Should not be case sensitive
    Function lowerCasePercentileFunction =
        intPercentileFunction.toBuilder().setFunctionName("percentile").build();
    assertEquals(
        expected,
        new PinotFunctionConverter()
            .convert(lowerCasePercentileFunction, this.mockArgumentConverter));

    // Should work with longs
    Function longPercentileFunction =
        intPercentileFunction.toBuilder()
            .setArguments(0, createLongLiteralValueExpression(90))
            .build();
    assertEquals(
        expected,
        new PinotFunctionConverter().convert(longPercentileFunction, this.mockArgumentConverter));
  }

  @Test
  void acceptsCustomPercentileFunctions() {
    String expected = "CUSTOMPERCENTILE90(foo)";
    Function percentileFunction =
        buildFunction(
            QUERY_FUNCTION_PERCENTILE,
            createIntLiteralValueExpression(90).toBuilder(),
            createColumnExpression("foo"));
    when(this.mockArgumentConverter.apply(any(Expression.class))).thenReturn("foo");

    assertEquals(
        expected,
        new PinotFunctionConverter("CUSTOMPERCENTILE")
            .convert(percentileFunction, this.mockArgumentConverter));
  }

  @Test
  void errorsOnInvalidPercentileValueArg() {
    Function percentileFunctionWithoutValue =
        buildFunction(QUERY_FUNCTION_PERCENTILE, createColumnExpression("foo"));

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            new PinotFunctionConverter()
                .convert(percentileFunctionWithoutValue, this.mockArgumentConverter));
  }

  @Test
  void backwardsCompatibleWithPercentileValueFunction() {
    String expected = "PERCENTILETDIGEST90(foo)";
    Function percentileFunction = buildFunction("PERCENTILE90", createColumnExpression("foo"));
    when(this.mockArgumentConverter.apply(any(Expression.class))).thenReturn("foo");

    assertEquals(
        expected,
        new PinotFunctionConverter().convert(percentileFunction, this.mockArgumentConverter));
  }

  @Test
  void convertsBasicFunctions() {
    PinotFunctionConverter converter = new PinotFunctionConverter();

    when(this.mockArgumentConverter.apply(any(Expression.class))).thenReturn("foo");
    assertEquals(
        "SUM(foo)",
        converter.convert(
            buildFunction(QUERY_FUNCTION_SUM, createColumnExpression("foo")),
            this.mockArgumentConverter));

    assertEquals(
        "AVG(foo)",
        converter.convert(
            buildFunction(QUERY_FUNCTION_AVG, createColumnExpression("foo")),
            this.mockArgumentConverter));
    assertEquals(
        "DISTINCTCOUNT(foo)",
        converter.convert(
            buildFunction(QUERY_FUNCTION_DISTINCTCOUNT, createColumnExpression("foo")),
            this.mockArgumentConverter));
    assertEquals(
        "MAX(foo)",
        converter.convert(
            buildFunction(QUERY_FUNCTION_MAX, createColumnExpression("foo")),
            this.mockArgumentConverter));
    assertEquals(
        "MIN(foo)",
        converter.convert(
            buildFunction(QUERY_FUNCTION_MIN, createColumnExpression("foo")),
            this.mockArgumentConverter));
  }

  @Test
  void convertsUnknownFunctions() {
    when(this.mockArgumentConverter.apply(any(Expression.class))).thenReturn("foo");
    assertEquals(
        "UNKNOWN(foo)",
        new PinotFunctionConverter()
            .convert(
                buildFunction("UNKNOWN", createColumnExpression("foo")),
                this.mockArgumentConverter));
  }

  @Test
  void convertsConcatFunctionAddingSeparator() {
    Expression column1 = createColumnExpression("foo").build();
    Expression column2 = createColumnExpression("bar").build();
    Expression separator = createStringLiteralValueExpression("");

    when(this.mockArgumentConverter.apply(column1)).thenReturn("foo");
    when(this.mockArgumentConverter.apply(column2)).thenReturn("bar");
    when(this.mockArgumentConverter.apply(separator)).thenReturn("''");
    assertEquals(
        "CONCAT(foo,bar,'')",
        new PinotFunctionConverter()
            .convert(
                buildFunction(QUERY_FUNCTION_CONCAT, column1.toBuilder(), column2.toBuilder()),
                this.mockArgumentConverter));
  }

  private Function buildFunction(String name, Builder... arguments) {
    return Function.newBuilder()
        .setFunctionName(name)
        .addAllArguments(Arrays.stream(arguments).map(Builder::build).collect(Collectors.toList()))
        .build();
  }
}
