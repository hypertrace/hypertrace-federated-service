package org.hypertrace.core.query.service;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.typesafe.config.Config;
import javax.inject.Singleton;
import org.hypertrace.core.attribute.service.cachingclient.CachingAttributeClient;
import org.hypertrace.core.query.service.api.QueryServiceGrpc.QueryServiceImplBase;
import org.hypertrace.core.query.service.pinot.PinotModule;
import org.hypertrace.core.query.service.projection.ProjectionModule;
import org.hypertrace.core.serviceframework.spi.PlatformServiceLifecycle;

class QueryServiceModule extends AbstractModule {

  private final QueryServiceConfig config;
  private final PlatformServiceLifecycle lifecycle;

  QueryServiceModule(Config config, PlatformServiceLifecycle lifecycle) {
    this.config = new QueryServiceConfig(config);
    this.lifecycle = lifecycle;
  }

  @Override
  protected void configure() {
    bind(QueryServiceConfig.class).toInstance(this.config);
    bind(PlatformServiceLifecycle.class).toInstance(this.lifecycle);
    bind(QueryServiceImplBase.class).to(QueryServiceImpl.class);
    Multibinder.newSetBinder(binder(), QueryTransformation.class);
    bind(CachingAttributeClient.class)
        .toProvider(AttributeClientProvider.class)
        .in(Singleton.class);
    install(new PinotModule());
    install(new ProjectionModule());
  }
}
