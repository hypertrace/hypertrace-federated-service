package org.hypertrace.graphql.entity;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.attributes.IdMappingLoader;
import org.hypertrace.core.graphql.utils.grpc.GraphQlGrpcContextBuilder;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.graphql.config.HypertraceGraphQlServiceConfig;

public class EntityIdModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), IdMappingLoader.class)
        .addBinding()
        .to(EntityIdMappingLoader.class);
    requireBinding(HypertraceGraphQlServiceConfig.class);
    requireBinding(GrpcChannelRegistry.class);
    requireBinding(GraphQlGrpcContextBuilder.class);
  }
}
