package org.hypertrace.graphql.utils.metrics.gateway;

import static io.reactivex.rxjava3.core.Single.zip;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderDirection;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.common.SortOrder;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

class AggregatableOrderByExpressionListConverter
    implements Converter<
        List<AttributeAssociation<AggregatableOrderArgument>>, List<OrderByExpression>> {

  private final Converter<AttributeModel, Expression> columnExpressionConverter;
  private final MetricAggregationExpressionConverter metricAggregationExpressionConverter;
  private final Converter<OrderDirection, SortOrder> sortOrderConverter;
  private final Converter<MetricAggregationType, AttributeModelMetricAggregationType>
      aggregationTypeConverter;

  @Inject
  AggregatableOrderByExpressionListConverter(
      Converter<AttributeModel, Expression> columnExpressionConverter,
      MetricAggregationExpressionConverter metricAggregationExpressionConverter,
      Converter<OrderDirection, SortOrder> sortOrderConverter,
      Converter<MetricAggregationType, AttributeModelMetricAggregationType>
          aggregationTypeConverter) {
    this.columnExpressionConverter = columnExpressionConverter;
    this.metricAggregationExpressionConverter = metricAggregationExpressionConverter;
    this.sortOrderConverter = sortOrderConverter;
    this.aggregationTypeConverter = aggregationTypeConverter;
  }

  @Override
  public Single<List<OrderByExpression>> convert(
      List<AttributeAssociation<AggregatableOrderArgument>> orders) {
    return Observable.fromIterable(orders)
        .flatMapSingle(this::buildOrderByExpression)
        .collect(Collectors.toUnmodifiableList());
  }

  private Single<OrderByExpression> buildOrderByExpression(
      AttributeAssociation<AggregatableOrderArgument> orderArgument) {
    return zip(
        this.sortOrderConverter.convert(orderArgument.value().direction()),
        this.buildSelectionExpression(orderArgument),
        (sortOrder, expression) ->
            OrderByExpression.newBuilder().setOrder(sortOrder).setExpression(expression).build());
  }

  private Single<Expression> buildSelectionExpression(
      AttributeAssociation<AggregatableOrderArgument> orderArgument) {
    return Maybe.fromOptional(Optional.ofNullable(orderArgument.value().aggregation()))
        .flatMapSingle(this.aggregationTypeConverter::convert)
        .flatMapSingle(
            aggregationType -> orderArgument.value().size() == null ?
                this.metricAggregationExpressionConverter.convertForNoArgsOrAlias(
                    orderArgument.attribute(), aggregationType) :
                this.metricAggregationExpressionConverter.convertForArgsButNoAlias(
                    orderArgument.attribute(), aggregationType, List.of(Objects.requireNonNull(orderArgument.value().size())))
            )
        .switchIfEmpty(this.columnExpressionConverter.convert(orderArgument.attribute()));
  }
}
