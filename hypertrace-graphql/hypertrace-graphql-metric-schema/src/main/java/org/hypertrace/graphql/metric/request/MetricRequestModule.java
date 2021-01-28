package org.hypertrace.graphql.metric.request;

import com.google.inject.AbstractModule;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;

public class MetricRequestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(MetricRequestBuilder.class).to(DefaultMetricRequestBuilder.class);
    bind(MetricAggregationRequestBuilder.class).to(DefaultMetricAggregationRequestBuilder.class);

    requireBinding(AttributeStore.class);
    requireBinding(ArgumentDeserializer.class);
    requireBinding(GraphQlSelectionFinder.class);
  }
}
