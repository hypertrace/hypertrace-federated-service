package org.hypertrace.core.bootstrapper;

import com.typesafe.config.Config;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.List;
import org.hypertrace.core.attribute.service.client.AttributeServiceClient;
import org.hypertrace.core.attribute.service.client.config.AttributeServiceClientConfig;
import org.hypertrace.entity.service.client.config.EntityServiceClientConfig;
import org.hypertrace.entity.type.service.client.EntityTypeServiceClient;

/**
 * Config passed to every command. Encapsulates all the clients used by the bootstrapper
 */
public class BootstrapContext {

  private final AttributeServiceClient attributesServiceClient;
  private final EntityTypeServiceClient entityTypeServiceClient;
  private final org.hypertrace.entity.type.client.EntityTypeServiceClient entityTypeServiceClientV2;

  private final List<ManagedChannel> managedChannels;

  private BootstrapContext(
      AttributeServiceClientConfig attributeServiceClientConfig,
      EntityServiceClientConfig entityServiceClientConfig) {

    ManagedChannel attributeServiceChannel = ManagedChannelBuilder.forAddress(
        attributeServiceClientConfig.getHost(), attributeServiceClientConfig.getPort())
        .usePlaintext().build();
    this.attributesServiceClient = new AttributeServiceClient(attributeServiceChannel);

    ManagedChannel entityServiceChannel = ManagedChannelBuilder.forAddress(
        entityServiceClientConfig.getHost(), entityServiceClientConfig.getPort())
        .usePlaintext().build();
    this.entityTypeServiceClient = new EntityTypeServiceClient(entityServiceChannel);
    this.entityTypeServiceClientV2 =
        new org.hypertrace.entity.type.client.EntityTypeServiceClient(entityServiceChannel);

    managedChannels = List.of(attributeServiceChannel, entityServiceChannel);
  }

  public static BootstrapContext buildFrom(Config config) {
    return new BootstrapContext(
        AttributeServiceClientConfig.from(config), EntityServiceClientConfig.from(config));
  }

  public AttributeServiceClient getAttributesServiceClient() {
    return attributesServiceClient;
  }

  public EntityTypeServiceClient getEntityTypeServiceClient() {
    return entityTypeServiceClient;
  }

  public org.hypertrace.entity.type.client.EntityTypeServiceClient getEntityTypeServiceClientV2() {
    return entityTypeServiceClientV2;
  }

  public void close() {
    managedChannels.forEach((managedChannel) -> {
      if(!managedChannel.isShutdown()) {
        managedChannel.shutdown();
      }
    });
  }
}
