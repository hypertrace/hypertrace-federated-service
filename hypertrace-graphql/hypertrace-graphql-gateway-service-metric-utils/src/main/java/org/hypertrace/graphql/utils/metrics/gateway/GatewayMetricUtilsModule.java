package org.hypertrace.graphql.utils.metrics.gateway;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderDirection;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.common.utils.TriConverter;
import org.hypertrace.gateway.service.v1.baseline.BaselineEntity;
import org.hypertrace.gateway.service.v1.common.AggregatedMetricValue;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.common.SortOrder;
import org.hypertrace.gateway.service.v1.common.TimeAggregation;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.entity.Entity;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.BaselinedMetricAggregationContainer;
import org.hypertrace.graphql.metric.schema.MetricContainer;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

public class GatewayMetricUtilsModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Key.get(
            new TypeLiteral<Converter<Collection<MetricAggregationRequest>, Set<Expression>>>() {}))
        .to(MetricAggregationExpressionConverter.class);

    bind(Key.get(
            new TypeLiteral<Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>>>() {}))
        .to(MetricSeriesExpressionConverter.class);

    bind(Key.get(
            new TypeLiteral<
                TriConverter<
                    Collection<MetricRequest>,
                    Entity,
                    BaselineEntity,
                    Map<String, MetricContainer>>>() {}))
        .to(MetricContainerMapConverter.class);
    bind(Key.get(
            new TypeLiteral<
                BiConverter<
                    Collection<MetricAggregationRequest>,
                    Map<String, AggregatedMetricValue>,
                    Map<String, BaselinedMetricAggregationContainer>>>() {}))
        .to(MetricAggregationContainerMapConverter.class);

    bind(Key.get(
            new TypeLiteral<
                Converter<
                    List<AttributeAssociation<AggregatableOrderArgument>>,
                    List<OrderByExpression>>>() {}))
        .to(AggregatableOrderByExpressionListConverter.class);

    requireBinding(Key.get(new TypeLiteral<Converter<AttributeModel, Expression>>() {}));
    requireBinding(Key.get(new TypeLiteral<Converter<Value, Object>>() {}));
    requireBinding(Key.get(new TypeLiteral<Converter<Object, Expression>>() {}));
    requireBinding(Key.get(new TypeLiteral<Converter<OrderDirection, SortOrder>>() {}));
    requireBinding(
        Key.get(
            new TypeLiteral<
                Converter<MetricAggregationType, AttributeModelMetricAggregationType>>() {}));
  }
}
