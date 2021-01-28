package org.hypertrace.graphql.explorer.request;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.common.request.AttributeRequestBuilder;
import org.hypertrace.core.graphql.common.request.FilterRequestBuilder;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.core.graphql.common.utils.attributes.AttributeAssociator;
import org.hypertrace.core.graphql.common.utils.attributes.AttributeScopeStringTranslator;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializer;
import org.hypertrace.core.graphql.utils.schema.GraphQlSelectionFinder;
import org.hypertrace.graphql.metric.request.MetricAggregationRequestBuilder;

public class ExploreRequestModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ExploreRequestBuilder.class).to(DefaultExploreRequestBuilder.class);
    requireBinding(AttributeStore.class);
    requireBinding(ArgumentDeserializer.class);
    requireBinding(AttributeAssociator.class);
    requireBinding(GraphQlSelectionFinder.class);
    requireBinding(AttributeRequestBuilder.class);
    requireBinding(MetricAggregationRequestBuilder.class);
    requireBinding(FilterRequestBuilder.class);
    requireBinding(
        Key.get(
            new TypeLiteral<
                Converter<MetricAggregationType, AttributeModelMetricAggregationType>>() {}));
    requireBinding(AttributeScopeStringTranslator.class);
  }
}
