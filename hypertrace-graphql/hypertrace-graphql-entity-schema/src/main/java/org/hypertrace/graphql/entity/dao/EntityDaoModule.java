package org.hypertrace.graphql.entity.dao;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.grpc.CallCredentials;
import io.reactivex.rxjava3.core.Scheduler;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.results.arguments.filter.FilterArgument;
import org.hypertrace.core.graphql.common.utils.BiConverter;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.common.utils.TriConverter;
import org.hypertrace.core.graphql.context.GraphQlRequestContextBuilder;
import org.hypertrace.core.graphql.rx.BoundedIoScheduler;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.gateway.service.v1.baseline.BaselineEntity;
import org.hypertrace.gateway.service.v1.common.AggregatedMetricValue;
import org.hypertrace.gateway.service.v1.common.Expression;
import org.hypertrace.gateway.service.v1.common.Filter;
import org.hypertrace.gateway.service.v1.common.OrderByExpression;
import org.hypertrace.gateway.service.v1.common.TimeAggregation;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.entity.Entity;
import org.hypertrace.graphql.entity.health.BaselineDao;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricRequest;
import org.hypertrace.graphql.metric.request.MetricSeriesRequest;
import org.hypertrace.graphql.metric.schema.BaselinedMetricAggregationContainer;
import org.hypertrace.graphql.metric.schema.MetricContainer;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

public class EntityDaoModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(EntityDao.class).to(GatewayServiceEntityDao.class);
    bind(BaselineDao.class).to(GatewayBaselineDao.class);
    requireBinding(CallCredentials.class);
    requireBinding(GraphQlServiceConfig.class);
    requireBinding(GraphQlRequestContextBuilder.class);
    requireBinding(GrpcChannelRegistry.class);
    requireBinding(Key.get(Scheduler.class, BoundedIoScheduler.class));

    requireBinding(
        Key.get(new TypeLiteral<Converter<Collection<AttributeRequest>, Set<Expression>>>() {}));

    requireBinding(
        Key.get(
            new TypeLiteral<
                Converter<
                    List<AttributeAssociation<AggregatableOrderArgument>>,
                    List<OrderByExpression>>>() {}));

    requireBinding(
        Key.get(
            new TypeLiteral<
                Converter<Collection<AttributeAssociation<FilterArgument>>, Filter>>() {}));

    requireBinding(
        Key.get(
            new TypeLiteral<
                Converter<Collection<MetricAggregationRequest>, Set<Expression>>>() {}));

    requireBinding(
        Key.get(
            new TypeLiteral<
                Converter<Collection<MetricSeriesRequest>, Set<TimeAggregation>>>() {}));

    requireBinding(
        Key.get(
            new TypeLiteral<
                TriConverter<
                    Collection<MetricRequest>, Entity, BaselineEntity, Map<String, MetricContainer>>>() {}));

    requireBinding(
        Key.get(
            new TypeLiteral<
                BiConverter<
                    Collection<AttributeRequest>, Map<String, Value>, Map<String, Object>>>() {}));
    requireBinding(
        Key.get(
            new TypeLiteral<
                BiConverter<
                    Collection<MetricAggregationRequest>,
                    Map<String, AggregatedMetricValue>,
                    Map<String, BaselinedMetricAggregationContainer>>>() {}));
  }
}
