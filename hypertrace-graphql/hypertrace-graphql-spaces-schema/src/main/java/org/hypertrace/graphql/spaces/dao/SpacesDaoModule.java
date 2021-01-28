package org.hypertrace.graphql.spaces.dao;

import com.google.inject.AbstractModule;
import io.grpc.CallCredentials;
import java.time.Clock;
import org.hypertrace.core.graphql.common.request.AttributeRequestBuilder;
import org.hypertrace.core.graphql.context.GraphQlRequestContextBuilder;
import org.hypertrace.core.graphql.utils.grpc.GrpcChannelRegistry;
import org.hypertrace.graphql.config.HypertraceGraphQlServiceConfig;
import org.hypertrace.graphql.explorer.dao.ExplorerDao;
import org.hypertrace.graphql.metric.request.MetricAggregationRequestBuilder;

public class SpacesDaoModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SpacesConfigDao.class).to(ConfigServiceSpacesConfigDao.class);
    bind(SpacesDao.class).to(ExplorerBackedSpacesDao.class);
    requireBinding(CallCredentials.class);
    requireBinding(HypertraceGraphQlServiceConfig.class);
    requireBinding(GraphQlRequestContextBuilder.class);
    requireBinding(GrpcChannelRegistry.class);
    requireBinding(ExplorerDao.class);
    requireBinding(AttributeRequestBuilder.class);
    requireBinding(MetricAggregationRequestBuilder.class);
    requireBinding(Clock.class);
  }
}
