package org.hypertrace.graphql.service;

import org.eclipse.jetty.server.Server;
import org.hypertrace.core.serviceframework.PlatformService;
import org.hypertrace.core.serviceframework.config.ConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQlService extends PlatformService {
  private static final Logger LOG = LoggerFactory.getLogger(GraphQlService.class);

  private Server server;
  private GraphQlServiceImpl graphQlServiceImpl;

  public GraphQlService(ConfigClient configClient) {
    super(configClient);
  }

  @Override
  protected void doInit() {
    graphQlServiceImpl = new GraphQlServiceImpl(this.getAppConfig());
    server = new Server(graphQlServiceImpl.getGraphQlServiceConfig().getServicePort());
    server.setHandler(graphQlServiceImpl.getContextHandler());
    server.setStopAtShutdown(true);
  }

  @Override
  protected void doStart() {
    LOG.info("Starting service: {}", this.getServiceName());
    try {
      server.start();
    } catch (Exception e) {
      LOG.error("Failed to start service: {}", this.getServiceName());
      throw new RuntimeException(e);
    }

    try {
      server.join();
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(ie);
    }
  }

  @Override
  protected void doStop() {
    LOG.info("Shutting down service: {}", this.getServiceName());

    graphQlServiceImpl.shutdown();

    while (!server.isStopped()) {
      try {
        server.stop();
      } catch (Exception e) {
        LOG.error("Failed to shutdown service: {}", this.getServiceName());
        throw new RuntimeException(e);
      }
    }
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignore) {
    }
  }

  @Override
  public boolean healthCheck() {
    return true;
  }
}
