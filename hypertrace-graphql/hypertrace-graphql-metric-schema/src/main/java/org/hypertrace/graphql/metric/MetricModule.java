package org.hypertrace.graphql.metric;

import com.google.inject.AbstractModule;
import org.hypertrace.graphql.metric.deserialization.MetricDeserializationModule;
import org.hypertrace.graphql.metric.request.MetricRequestModule;

public class MetricModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new MetricDeserializationModule());
    install(new MetricRequestModule());
  }
}
