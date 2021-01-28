package org.hypertrace.core.query.service.projection;

import static io.reactivex.rxjava3.core.Single.zip;
import static org.hypertrace.core.query.service.QueryRequestUtil.createBooleanLiteralExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createColumnExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createDoubleLiteralExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createLongLiteralExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createNullNumberLiteralExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createNullStringLiteralExpression;
import static org.hypertrace.core.query.service.QueryRequestUtil.createStringLiteralExpression;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.hypertrace.core.attribute.service.cachingclient.CachingAttributeClient;
import org.hypertrace.core.attribute.service.projection.AttributeProjection;
import org.hypertrace.core.attribute.service.projection.AttributeProjectionRegistry;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.LiteralValue;
import org.hypertrace.core.attribute.service.v1.Projection;
import org.hypertrace.core.attribute.service.v1.ProjectionExpression;
import org.hypertrace.core.attribute.service.v1.ProjectionOperator;
import org.hypertrace.core.query.service.QueryFunctionConstants;
import org.hypertrace.core.query.service.QueryTransformation;
import org.hypertrace.core.query.service.api.ColumnIdentifier;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.Function;
import org.hypertrace.core.query.service.api.OrderByExpression;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ProjectionTransformation implements QueryTransformation {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectionTransformation.class);

  private final CachingAttributeClient attributeClient;
  private final AttributeProjectionRegistry projectionRegistry;

  @Inject
  ProjectionTransformation(
      CachingAttributeClient attributeClient, AttributeProjectionRegistry projectionRegistry) {
    this.attributeClient = attributeClient;
    this.projectionRegistry = projectionRegistry;
  }

  @Override
  public Single<QueryRequest> transform(
      QueryRequest queryRequest, QueryTransformationContext transformationContext) {
    return zip(
            this.transformExpressionList(queryRequest.getSelectionList()),
            this.transformExpressionList(queryRequest.getAggregationList()),
            this.transformFilter(queryRequest.getFilter()),
            this.transformExpressionList(queryRequest.getGroupByList()),
            this.transformOrderByList(queryRequest.getOrderByList()),
            (selections, aggregations, filter, groupBys, orderBys) ->
                this.rebuildRequestOmittingDefaults(
                    queryRequest, selections, aggregations, filter, groupBys, orderBys))
        .doOnSuccess(transformed -> this.debugLogIfRequestTransformed(queryRequest, transformed));
  }

  private Single<List<Expression>> transformExpressionList(List<Expression> expressionList) {
    return Observable.fromIterable(expressionList)
        .concatMapSingle(this::transformExpression)
        .toList();
  }

  private Single<Expression> transformExpression(Expression expression) {
    switch (expression.getValueCase()) {
      case COLUMNIDENTIFIER:
        return this.transformColumnIdentifier(expression.getColumnIdentifier());
      case FUNCTION:
        return this.transformFunction(expression.getFunction())
            .map(expression.toBuilder()::setFunction)
            .map(Expression.Builder::build);
      case ORDERBY:
        return this.transformOrderBy(expression.getOrderBy())
            .map(expression.toBuilder()::setOrderBy)
            .map(Expression.Builder::build);
      case LITERAL:
      case VALUE_NOT_SET:
      default:
        return Single.just(expression);
    }
  }

  private Single<Expression> transformColumnIdentifier(ColumnIdentifier columnIdentifier) {
    return this.projectAttributeIfPossible(columnIdentifier.getColumnName())
        .map(expression -> this.aliasToMatchOriginal(columnIdentifier, expression))
        .defaultIfEmpty(Expression.newBuilder().setColumnIdentifier(columnIdentifier).build());
  }

  private Single<Function> transformFunction(Function function) {
    return this.transformExpressionList(function.getArgumentsList())
        .map(expressions -> function.toBuilder().clearArguments().addAllArguments(expressions))
        .map(Function.Builder::build);
  }

  private Single<List<OrderByExpression>> transformOrderByList(
      List<OrderByExpression> orderByList) {
    return Observable.fromIterable(orderByList).concatMapSingle(this::transformOrderBy).toList();
  }

  private Single<OrderByExpression> transformOrderBy(OrderByExpression orderBy) {
    return this.transformExpression(orderBy.getExpression())
        .map(orderBy.toBuilder()::setExpression)
        .map(OrderByExpression.Builder::build);
  }

  private Single<Filter> transformFilter(Filter filter) {
    if (filter.equals(Filter.getDefaultInstance())) {
      return Single.just(filter);
    }

    Single<Expression> lhsSingle = this.transformExpression(filter.getLhs());
    Single<Expression> rhsSingle = this.transformExpression(filter.getRhs());
    Single<List<Filter>> childFilterListSingle =
        Observable.fromIterable(filter.getChildFilterList())
            .concatMapSingle(this::transformFilter)
            .toList();
    return zip(
        lhsSingle,
        rhsSingle,
        childFilterListSingle,
        (lhs, rhs, childFilterList) ->
            this.rebuildFilterOmittingDefaults(filter, lhs, rhs, childFilterList));
  }

  private Maybe<Expression> projectAttributeIfPossible(String attributeId) {
    return this.attributeClient
        .get(attributeId)
        .onErrorComplete()
        .flatMap(this::projectAttributeIfPossible);
  }

  private Maybe<Expression> projectAttributeIfPossible(AttributeMetadata attributeMetadata) {
    if (!attributeMetadata.getDefinition().hasProjection()) {
      return Maybe.empty();
    }
    return this.rewriteProjectionAsQueryExpression(
            attributeMetadata.getDefinition().getProjection(), attributeMetadata.getValueKind())
        .toMaybe();
  }

  private Single<Expression> rewriteProjectionAsQueryExpression(
      Projection projection, AttributeKind expectedType) {
    switch (projection.getValueCase()) {
      case ATTRIBUTE_ID:
        return this.transformExpression(createColumnExpression(projection.getAttributeId()));
      case LITERAL:
        return this.rewriteLiteralAsQueryExpression(projection.getLiteral(), expectedType);
      case EXPRESSION:
        return this.rewriteProjectionExpressionAsQueryExpression(projection.getExpression());
      case VALUE_NOT_SET:
      default:
        return Single.error(
            new UnsupportedOperationException("Unrecognized projection: " + projection));
    }
  }

  private Single<Expression> rewriteLiteralAsQueryExpression(
      LiteralValue literal, AttributeKind expectedType) {
    switch (literal.getValueCase()) {
      case STRING_VALUE:
        return Single.just(createStringLiteralExpression(literal.getStringValue()));
      case BOOLEAN_VALUE:
        return Single.just(createBooleanLiteralExpression(literal.getBooleanValue()));
      case FLOAT_VALUE:
        return Single.just(createDoubleLiteralExpression(literal.getFloatValue()));
      case INT_VALUE:
        return Single.just(createLongLiteralExpression(literal.getIntValue()));
      case VALUE_NOT_SET:
        return this.getCorrespondingNullExpression(expectedType);
      default:
        return Single.error(
            new UnsupportedOperationException("Unrecognized literal type: " + literal));
    }
  }

  private Single<Expression> getCorrespondingNullExpression(AttributeKind attributeKind) {
    switch (attributeKind) {
      case TYPE_DOUBLE:
      case TYPE_DOUBLE_ARRAY:
      case TYPE_INT64:
      case TYPE_INT64_ARRAY:
      case TYPE_TIMESTAMP:
        return Single.just(createNullNumberLiteralExpression());
      case TYPE_BOOL:
      case TYPE_BOOL_ARRAY:
      case TYPE_STRING:
      case TYPE_STRING_ARRAY:
      case TYPE_STRING_MAP:
      case TYPE_BYTES:
        return Single.just(createNullStringLiteralExpression());
      case UNRECOGNIZED:
      case KIND_UNDEFINED:
      default:
        return Single.error(
            new UnsupportedOperationException(
                "Unrecognized attribute kind for null expression conversion: " + attributeKind));
    }
  }

  private Single<Expression> rewriteProjectionExpressionAsQueryExpression(
      ProjectionExpression projectionExpression) {
    List<Projection> arguments = projectionExpression.getArgumentsList();
    Optional<List<AttributeKind>> knownArgumentKinds =
        this.projectionRegistry
            .getProjection(projectionExpression.getOperator())
            .map(AttributeProjection::getArgumentKinds);

    Single<List<Expression>> argumentListSingle =
        Observable.range(0, arguments.size())
            .concatMapSingle(
                index ->
                    rewriteProjectionAsQueryExpression(
                        arguments.get(index),
                        knownArgumentKinds
                            .map(kinds -> kinds.get(index))
                            .orElse(AttributeKind.KIND_UNDEFINED)))
            .toList();

    Single<String> operatorSingle = this.convertOperator(projectionExpression.getOperator());

    return zip(
        argumentListSingle,
        operatorSingle,
        (argumentExpressions, operatorName) ->
            Expression.newBuilder()
                .setFunction(
                    Function.newBuilder()
                        .setFunctionName(operatorName)
                        .addAllArguments(argumentExpressions))
                .build());
  }

  private Single<String> convertOperator(ProjectionOperator operator) {
    switch (operator) {
      case PROJECTION_OPERATOR_CONCAT:
        return Single.just(QueryFunctionConstants.QUERY_FUNCTION_CONCAT);
      case PROJECTION_OPERATOR_HASH:
        return Single.just(QueryFunctionConstants.QUERY_FUNCTION_HASH);
      case PROJECTION_OPERATOR_STRING_EQUALS:
        return Single.just(QueryFunctionConstants.QUERY_FUNCTION_STRINGEQUALS);
      case PROJECTION_OPERATOR_CONDITIONAL:
        return Single.just(QueryFunctionConstants.QUERY_FUNCTION_CONDITIONAL);
      case PROJECTION_OPERATOR_UNSET:
      case UNRECOGNIZED:
      default:
        return Single.error(
            new UnsupportedOperationException("Unrecognized operator: " + operator));
    }
  }

  private Expression aliasToMatchOriginal(ColumnIdentifier original, Expression newExpression) {
    String originalKey =
        original.getAlias().isEmpty() ? original.getColumnName() : original.getAlias();
    switch (newExpression.getValueCase()) {
      case COLUMNIDENTIFIER:
        return newExpression.toBuilder()
            .setColumnIdentifier(
                newExpression.getColumnIdentifier().toBuilder().setAlias(originalKey))
            .build();
      case FUNCTION:
        return newExpression.toBuilder()
            .setFunction(newExpression.getFunction().toBuilder().setAlias(originalKey))
            .build();
      case ORDERBY: // Rest of expressions types don't support aliases
      case LITERAL:
      case VALUE_NOT_SET:
      default:
        return newExpression;
    }
  }

  private void debugLogIfRequestTransformed(QueryRequest original, QueryRequest transformed) {
    if (!original.equals(transformed)) {
      LOG.debug(
          "Request transformation occurred. Original request: {} Transformed Request: {}",
          original,
          transformed);
    }
  }

  /**
   * This doesn't change any functional behavior, but omits fields that aren't needed, shrinking the
   * object and keeping it equivalent to the source object for equality checks.
   */
  private Filter rebuildFilterOmittingDefaults(
      Filter original, Expression lhs, Expression rhs, List<Filter> childFilters) {
    Filter.Builder builder = original.toBuilder();

    if (Expression.getDefaultInstance().equals(lhs)) {
      builder.clearLhs();
    } else {
      builder.setLhs(lhs);
    }

    if (Expression.getDefaultInstance().equals(rhs)) {
      builder.clearRhs();
    } else {
      builder.setRhs(rhs);
    }

    return builder.clearChildFilter().addAllChildFilter(childFilters).build();
  }

  private QueryRequest rebuildRequestOmittingDefaults(
      QueryRequest original,
      List<Expression> selections,
      List<Expression> aggregations,
      Filter filter,
      List<Expression> groupBys,
      List<OrderByExpression> orderBys) {
    QueryRequest.Builder builder = original.toBuilder();

    if (Filter.getDefaultInstance().equals(filter)) {
      builder.clearFilter();
    } else {
      builder.setFilter(filter);
    }

    return builder
        .clearSelection()
        .addAllSelection(selections)
        .clearAggregation()
        .addAllAggregation(aggregations)
        .clearGroupBy()
        .addAllGroupBy(groupBys)
        .clearOrderBy()
        .addAllOrderBy(orderBys)
        .build();
  }
}
