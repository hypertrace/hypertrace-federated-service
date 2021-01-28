package org.hypertrace.graphql.config;

import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;

public interface HypertraceGraphQlServiceConfig extends GraphQlServiceConfig {
  String getEntityServiceHost();

  int getEntityServicePort();

  String getConfigServiceHost();

  int getConfigServicePort();
}
