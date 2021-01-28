package org.hypertrace.core.query.service.client;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Config object used to pass the QueryService details that are to be used by the EntityGateway. */
public class QueryServiceConfig {
  private static final Logger LOG = LoggerFactory.getLogger(QueryServiceConfig.class);

  private final String queryServiceHost;
  private final int queryServicePort;

  public QueryServiceConfig(Config config) {
    LOG.info(config.toString());
    this.queryServiceHost = config.getString("host");
    this.queryServicePort = config.getInt("port");
  }

  public String getQueryServiceHost() {
    return this.queryServiceHost;
  }

  public int getQueryServicePort() {
    return queryServicePort;
  }
}
