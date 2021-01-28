package org.hypertrace.config.service;

import com.typesafe.config.Config;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import org.hypertrace.config.service.store.ConfigStore;
import org.hypertrace.config.service.store.DocumentConfigStore;
import org.hypertrace.core.serviceframework.spi.PlatformServiceLifecycle;
import org.hypertrace.space.config.service.SpacesConfigServiceImpl;

public class ConfigServicesFactory {
  private static final String GENERIC_CONFIG_SERVICE_CONFIG = "generic.config.service";

  public static List<BindableService> buildAllConfigServices(
      Config config, int port, PlatformServiceLifecycle lifecycle) {
    return buildAllConfigServices(ConfigServicesFactory.buildConfigStore(config), port, lifecycle);
  }

  public static List<BindableService> buildAllConfigServices(
      ConfigStore configStore, int port, PlatformServiceLifecycle lifecycle) {
    ManagedChannel configChannel =
        ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
    lifecycle.shutdownComplete().thenRun(configChannel::shutdown);

    return List.of(
        new ConfigServiceGrpcImpl(configStore), new SpacesConfigServiceImpl(configChannel));
  }

  public static ConfigStore buildConfigStore(Config config) {
    try {
      ConfigStore configStore = new DocumentConfigStore();
      configStore.init(config.getConfig(GENERIC_CONFIG_SERVICE_CONFIG));
      return configStore;
    } catch (Exception e) {
      throw new RuntimeException("Error in getting or initializing config store", e);
    }
  }
}
