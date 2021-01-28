package org.hypertrace.core.query.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import org.hypertrace.core.grpcutils.server.InterceptorUtil;
import org.hypertrace.core.serviceframework.PlatformService;
import org.hypertrace.core.serviceframework.config.ConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryServiceStarter extends PlatformService {
  private static final String SERVICE_NAME_CONFIG = "service.name";
  private static final String SERVICE_PORT_CONFIG = "service.port";
  private static final String QUERY_SERVICE_CONFIG = "service.config";
  private static final Logger LOG = LoggerFactory.getLogger(QueryServiceStarter.class);
  private String serviceName;
  private int serverPort;
  private Server queryServiceServer;

  public QueryServiceStarter(ConfigClient configClient) {
    super(configClient);
  }

  @Override
  protected void doInit() {
    this.serviceName = getAppConfig().getString(SERVICE_NAME_CONFIG);
    this.serverPort = getAppConfig().getInt(SERVICE_PORT_CONFIG);

    LOG.info("Creating the Query Service Server on port {}", serverPort);

    queryServiceServer =
        ServerBuilder.forPort(serverPort)
            .addService(
                InterceptorUtil.wrapInterceptors(
                    QueryServiceFactory.build(
                        getAppConfig().getConfig(QUERY_SERVICE_CONFIG), this.getLifecycle())))
            .build();
  }

  @Override
  protected void doStart() {
    LOG.info("Attempting to start Query Service on port {}", serverPort);

    try {
      queryServiceServer.start();
      LOG.info("Started Query Service on port {}", serverPort);
    } catch (IOException e) {
      LOG.error("Unable to start the Query Service");
      throw new RuntimeException(e);
    }

    try {
      queryServiceServer.awaitTermination();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doStop() {
    LOG.info("Shutting down service: {}", serviceName);
    while (!queryServiceServer.isShutdown()) {
      queryServiceServer.shutdown();
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignore) {
      }
    }
  }

  @Override
  public boolean healthCheck() {
    return true;
  }

  @Override
  public String getServiceName() {
    return serviceName;
  }
}
