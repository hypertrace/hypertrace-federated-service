package org.hypertrace.core.query.service;

import static org.hypertrace.core.grpcutils.client.RequestContextClientCallCredsProviderFactory.getClientCallCredsProvider;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import org.hypertrace.core.attribute.service.cachingclient.CachingAttributeClient;
import org.hypertrace.core.serviceframework.spi.PlatformServiceLifecycle;

final class AttributeClientProvider implements Provider<CachingAttributeClient> {

  private final QueryServiceConfig config;
  private final PlatformServiceLifecycle serviceLifecycle;

  @Inject
  AttributeClientProvider(QueryServiceConfig config, PlatformServiceLifecycle serviceLifecycle) {
    this.config = config;
    this.serviceLifecycle = serviceLifecycle;
  }

  @Override
  public CachingAttributeClient get() {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress(
                config.getAttributeClientConfig().getHost(),
                config.getAttributeClientConfig().getPort())
            .usePlaintext()
            .build();

    this.serviceLifecycle.shutdownComplete().thenRun(channel::shutdown);

    return CachingAttributeClient.builder(channel)
        .withCallCredentials(getClientCallCredsProvider().get())
        .withCacheExpiration(Duration.of(15, ChronoUnit.MINUTES))
        .build();
  }
}
