package org.hypertrace.core.query.service;

import com.google.inject.Guice;
import com.typesafe.config.Config;
import org.hypertrace.core.query.service.api.QueryServiceGrpc.QueryServiceImplBase;
import org.hypertrace.core.serviceframework.spi.PlatformServiceLifecycle;

public class QueryServiceFactory {

  public static QueryServiceImplBase build(
      Config config, PlatformServiceLifecycle serviceLifecycle) {
    return Guice.createInjector(new QueryServiceModule(config, serviceLifecycle))
        .getInstance(QueryServiceImplBase.class);
  }
}
