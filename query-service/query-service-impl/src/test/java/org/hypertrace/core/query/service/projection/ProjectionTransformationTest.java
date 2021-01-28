package org.hypertrace.core.query.service.projection;

import static java.util.Arrays.asList;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_CONCAT;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_CONDITIONAL;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_HASH;
import static org.hypertrace.core.attribute.service.v1.ProjectionOperator.PROJECTION_OPERATOR_STRING_EQUALS;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_AVG;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_CONCAT;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_CONDITIONAL;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_HASH;
import static org.hypertrace.core.query.service.QueryFunctionConstants.QUERY_FUNCTION_STRINGEQUALS;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createAliasedColumnExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createColumnExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createCompositeFilter;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createEqualsFilter;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createFilter;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createFunctionExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createInFilter;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createOrderByExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createStringArrayLiteralValueExpression;
import static org.hypertrace.core.query.service.QueryRequestBuilderUtils.createStringLiteralValueExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createNullNumberLiteralExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createNullStringLiteralExpression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hypertrace.core.attribute.service.cachingclient.CachingAttributeClient;
import org.hypertrace.core.attribute.service.projection.AttributeProjectionRegistry;
import org.hypertrace.core.attribute.service.v1.AttributeDefinition;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.LiteralValue;
import org.hypertrace.core.attribute.service.v1.Projection;
import org.hypertrace.core.attribute.service.v1.ProjectionExpression;
import org.hypertrace.core.attribute.service.v1.ProjectionOperator;
import org.hypertrace.core.query.service.QueryTransformation.QueryTransformationContext;
import org.hypertrace.core.query.service.api.Operator;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.SortOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectionTransformationTest {

  private static final String PROJECTED_ATTRIBUTE_ID = "PROJECTED_ATTRIBUTE_ID";
  private static final String SIMPLE_ATTRIBUTE_ID = "SIMPLE_ATTRIBUTE_ID";

  @Mock CachingAttributeClient mockAttributeClient;
  @Mock QueryTransformationContext mockTransformationContext;
  private AttributeMetadata attributeMetadata;

  ProjectionTransformation projectionTransformation;

  @BeforeEach
  void beforeEach() {
    this.attributeMetadata =
        AttributeMetadata.newBuilder()
            .setDefinition(
                AttributeDefinition.newBuilder()
                    .setProjection(Projection.newBuilder().setAttributeId(SIMPLE_ATTRIBUTE_ID)))
            .build();

    when(this.mockAttributeClient.get(PROJECTED_ATTRIBUTE_ID))
        .thenReturn(Single.defer(() -> Single.just(this.attributeMetadata)));
    when(this.mockAttributeClient.get(SIMPLE_ATTRIBUTE_ID))
        .thenReturn(Single.defer(() -> Single.just(AttributeMetadata.getDefaultInstance())));

    this.projectionTransformation =
        new ProjectionTransformation(this.mockAttributeClient, new AttributeProjectionRegistry());
  }

  @Test
  void transformsBasicAliasProjection() {
    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .build();
    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addSelection(
                createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformsComplexFunctionProjection() {
    // CONCAT(HASH(SIMPLE_ATTRIBUTE_ID), "projectionLiteral")
    Projection projection =
        functionProjection(
            projectionExpression(
                PROJECTION_OPERATOR_CONCAT,
                functionProjection(
                    projectionExpression(
                        PROJECTION_OPERATOR_HASH, attributeIdProjection(SIMPLE_ATTRIBUTE_ID))),
                literalProjection("projectionLiteral")));

    this.attributeMetadata =
        this.attributeMetadata.toBuilder()
            .setDefinition(AttributeDefinition.newBuilder().setProjection(projection))
            .build();

    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .build();

    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addSelection(
                createFunctionExpression(
                    QUERY_FUNCTION_CONCAT,
                    PROJECTED_ATTRIBUTE_ID,
                    createFunctionExpression(
                        QUERY_FUNCTION_HASH, createColumnExpression(SIMPLE_ATTRIBUTE_ID).build()),
                    createStringLiteralValueExpression("projectionLiteral")))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformsConditionalAndStringEquals() {
    // CONDITIONAL(STRINGEQUALS(SIMPLE_ATTRIBUTE_ID, "foo"), HASH(SIMPLE_ATTRIBUTE_ID), "projectionLiteral")
    Projection projection =
        functionProjection(
            projectionExpression(
                PROJECTION_OPERATOR_CONDITIONAL,
                functionProjection(
                    projectionExpression(
                        PROJECTION_OPERATOR_STRING_EQUALS,
                        attributeIdProjection(SIMPLE_ATTRIBUTE_ID),
                        literalProjection("foo"))),
                functionProjection(
                    projectionExpression(
                        PROJECTION_OPERATOR_HASH,
                        attributeIdProjection(SIMPLE_ATTRIBUTE_ID))),
                literalProjection("projectionLiteral")));

    this.attributeMetadata =
        this.attributeMetadata.toBuilder()
            .setDefinition(AttributeDefinition.newBuilder().setProjection(projection))
            .build();

    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .build();

    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addSelection(
                createFunctionExpression(
                    QUERY_FUNCTION_CONDITIONAL,
                    PROJECTED_ATTRIBUTE_ID,
                    createFunctionExpression(
                        QUERY_FUNCTION_STRINGEQUALS,
                        createColumnExpression(SIMPLE_ATTRIBUTE_ID).build(),
                        createStringLiteralValueExpression("foo")),
                    createFunctionExpression(
                        QUERY_FUNCTION_HASH, createColumnExpression(SIMPLE_ATTRIBUTE_ID).build()),
                    createStringLiteralValueExpression("projectionLiteral")))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformsAggregations() {
    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addAggregation(
                createFunctionExpression(
                    QUERY_FUNCTION_AVG,
                    "myAlias",
                    createColumnExpression(PROJECTED_ATTRIBUTE_ID).build()))
            .build();
    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addAggregation(
                createFunctionExpression(
                    QUERY_FUNCTION_AVG,
                    "myAlias",
                    createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID)))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformsOrderBys() {
    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .addOrderBy(createOrderByExpression(PROJECTED_ATTRIBUTE_ID, SortOrder.DESC))
            .build();
    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addSelection(
                createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID))
            .addOrderBy(
                createOrderByExpression(
                    createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID)
                        .toBuilder(),
                    SortOrder.DESC))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformsNestedFilters() {
    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .setFilter(
                createCompositeFilter(
                    Operator.OR,
                    createInFilter(PROJECTED_ATTRIBUTE_ID, List.of("foo", "bar")),
                    createEqualsFilter(SIMPLE_ATTRIBUTE_ID, "otherValue")))
            .build();

    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addSelection(
                createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID))
            .setFilter(
                createCompositeFilter(
                    Operator.OR,
                    createFilter(
                        createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID),
                        Operator.IN,
                        createStringArrayLiteralValueExpression(List.of("foo", "bar"))),
                    createEqualsFilter(SIMPLE_ATTRIBUTE_ID, "otherValue")))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformsGroupBys() {
    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addAggregation(
                createFunctionExpression(
                    QUERY_FUNCTION_AVG, createColumnExpression(PROJECTED_ATTRIBUTE_ID).build()))
            .addGroupBy(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .build();
    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addAggregation(
                createFunctionExpression(
                    QUERY_FUNCTION_AVG,
                    createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID)))
            .addGroupBy(createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void passesThroughExpressionsInOrder() {
    when(this.mockAttributeClient.get("slow"))
        .thenReturn(
            Single.defer(
                () ->
                    Single.just(AttributeMetadata.getDefaultInstance())
                        .delay(10, TimeUnit.MILLISECONDS)));

    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression("slow"))
            .addSelection(createColumnExpression(SIMPLE_ATTRIBUTE_ID))
            .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .build();
    QueryRequest expected =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression("slow"))
            .addSelection(createColumnExpression(SIMPLE_ATTRIBUTE_ID))
            .addSelection(
                createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID))
            .build();
    assertEquals(
        expected,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void passesThroughOrderBysInOrder() {
    when(this.mockAttributeClient.get("slow"))
        .thenReturn(
            Single.defer(
                () ->
                    Single.just(AttributeMetadata.getDefaultInstance())
                        .delay(10, TimeUnit.MILLISECONDS)));

    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addOrderBy(createOrderByExpression("slow", SortOrder.ASC))
            .addOrderBy(createOrderByExpression(PROJECTED_ATTRIBUTE_ID, SortOrder.DESC))
            .build();
    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addOrderBy(createOrderByExpression("slow", SortOrder.ASC))
            .addOrderBy(
                createOrderByExpression(
                    createAliasedColumnExpression(SIMPLE_ATTRIBUTE_ID, PROJECTED_ATTRIBUTE_ID)
                        .toBuilder(),
                    SortOrder.DESC))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformNullDefinedAttributes() {
    this.attributeMetadata =
        this.attributeMetadata.toBuilder()
            .setDefinition(AttributeDefinition.newBuilder().setProjection(nullLiteralProjection()))
            .setValueKind(AttributeKind.TYPE_STRING)
            .build();

    QueryRequest originalRequest =
        QueryRequest.newBuilder()
            .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
            .addSelection(createColumnExpression(SIMPLE_ATTRIBUTE_ID))
            .build();

    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
            .addSelection(createNullStringLiteralExpression())
            .addSelection(createColumnExpression(SIMPLE_ATTRIBUTE_ID))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());

    // Now make it typed number
    this.attributeMetadata =
        this.attributeMetadata.toBuilder()
            .setDefinition(AttributeDefinition.newBuilder().setProjection(nullLiteralProjection()))
            .setValueKind(AttributeKind.TYPE_INT64)
            .build();

    expectedTransform =
        QueryRequest.newBuilder()
            .addSelection(createNullNumberLiteralExpression())
            .addSelection(createColumnExpression(SIMPLE_ATTRIBUTE_ID))
            .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());
  }

  @Test
  void transformsNullProjectionArguments() {
    // CONDITIONAL("true", SIMPLE_ATTRIBUTE_ID, "null")
    Projection projection =
        functionProjection(
            projectionExpression(
                PROJECTION_OPERATOR_CONDITIONAL,
                literalProjection("true"),
                attributeIdProjection(SIMPLE_ATTRIBUTE_ID),
                nullLiteralProjection()
            ));

    this.attributeMetadata =
        this.attributeMetadata.toBuilder()
                              .setDefinition(AttributeDefinition.newBuilder().setProjection(projection))
                              .build();

    QueryRequest originalRequest =
        QueryRequest.newBuilder()
                    .addSelection(createColumnExpression(PROJECTED_ATTRIBUTE_ID))
                    .build();

    QueryRequest expectedTransform =
        QueryRequest.newBuilder()
                    .addSelection(
                        createFunctionExpression(
                            QUERY_FUNCTION_CONDITIONAL,
                            PROJECTED_ATTRIBUTE_ID,
                            createStringLiteralValueExpression("true"),
                            createColumnExpression(SIMPLE_ATTRIBUTE_ID).build(),
                            createNullStringLiteralExpression()))
                    .build();

    assertEquals(
        expectedTransform,
        this.projectionTransformation
            .transform(originalRequest, mockTransformationContext)
            .blockingGet());

  }

  private ProjectionExpression projectionExpression(
      ProjectionOperator operator, Projection... arguments) {
    return ProjectionExpression.newBuilder()
        .setOperator(operator)
        .addAllArguments(asList(arguments))
        .build();
  }

  private Projection attributeIdProjection(String attributeId) {
    return Projection.newBuilder().setAttributeId(attributeId).build();
  }

  private Projection functionProjection(ProjectionExpression expression) {
    return Projection.newBuilder().setExpression(expression).build();
  }

  private Projection literalProjection(String value) {
    return Projection.newBuilder()
        .setLiteral(LiteralValue.newBuilder().setStringValue(value))
        .build();
  }

  private Projection nullLiteralProjection() {
    return Projection.newBuilder().setLiteral(LiteralValue.getDefaultInstance()).build();
  }
}
