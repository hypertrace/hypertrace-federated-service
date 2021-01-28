package org.hypertrace.core.attribute.service.client.config;

import com.typesafe.config.Config;

public class AttributeServiceClientConfig {
  private static final String ATTRIBUTE_SERVICE_CONFIG_KEY = "attributes.service.config";

  private final String host;
  private final int port;

  private AttributeServiceClientConfig(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public static AttributeServiceClientConfig from(Config config) {
    Config attributeServiceConfig = config.getConfig(ATTRIBUTE_SERVICE_CONFIG_KEY);
    return new AttributeServiceClientConfig(
        attributeServiceConfig.getString("host"), attributeServiceConfig.getInt("port"));
  }

  @Override
  public String toString() {
    return "AttributeServiceClientConfig{" + "host='" + host + '\'' + ", port=" + port + '}';
  }
}
