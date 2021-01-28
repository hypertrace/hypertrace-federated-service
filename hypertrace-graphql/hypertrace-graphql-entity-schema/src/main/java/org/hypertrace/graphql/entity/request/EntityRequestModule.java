package org.hypertrace.graphql.entity.request;

import com.google.inject.AbstractModule;
import org.hypertrace.core.graphql.common.request.AttributeRequestBuilder;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.request.ResultSetRequestBuilder;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.graphql.metric.request.MetricAggregationRequestBuilder;
import org.hypertrace.graphql.metric.request.MetricRequestBuilder;

public class EntityRequestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(EntityRequestBuilder.class).to(DefaultEntityRequestBuilder.class);
    requireBinding(ResultSetRequestBuilder.class);
    requireBinding(ArgumentDeserializer.class);
    requireBinding(MetricRequestBuilder.class);
    requireBinding(AttributeRequestBuilder.class);
    requireBinding(MetricAggregationRequestBuilder.class);
    requireBinding(GraphQlSelectionFinder.class);
    requireBinding(FilterRequestBuilder.class);
  }
}
