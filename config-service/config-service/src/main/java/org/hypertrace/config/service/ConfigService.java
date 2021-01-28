package org.hypertrace.config.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import org.hypertrace.config.service.store.ConfigStore;
import org.hypertrace.core.grpcutils.server.InterceptorUtil;
import org.hypertrace.core.serviceframework.PlatformService;
import org.hypertrace.core.serviceframework.config.ConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigService extends PlatformService {

  private static final String SERVICE_PORT_CONFIG = "service.port";
  private static final Logger LOG = LoggerFactory.getLogger(ConfigService.class);
  private int serverPort;
  private Server configServer;
  private ConfigStore configStore;

  public ConfigService(ConfigClient configClient) {
    super(configClient);
  }

  @Override
  protected void doInit() {
    serverPort = getAppConfig().getInt(SERVICE_PORT_CONFIG);
    LOG.info("Creating {} on port {}", getServiceName(), serverPort);

    ServerBuilder<?> serverBuilder = ServerBuilder.forPort(serverPort);

    configStore = ConfigServicesFactory.buildConfigStore(getAppConfig());
    ConfigServicesFactory.buildAllConfigServices(configStore, serverPort, getLifecycle())
        .stream()
        .map(InterceptorUtil::wrapInterceptors)
        .forEach(serverBuilder::addService);

    this.configServer = serverBuilder.build();
  }

  @Override
  protected void doStart() {
    LOG.info("Attempting to start {} on port {}", getServiceName(), serverPort);
    try {
      configServer.start();
      LOG.info("Started Config Service on port {}", serverPort);
    } catch (IOException e) {
      LOG.error("Unable to start Config Service");
      throw new RuntimeException(e);
    }

    try {
      configServer.awaitTermination();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doStop() {
    LOG.info("Shutting down service: {}", getServiceName());
    while (!configServer.isShutdown()) {
      configServer.shutdown();
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        LOG.warn("Interrupted!", e);
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public boolean healthCheck() {
    return configStore.healthCheck();
  }
}
