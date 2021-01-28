package org.hypertrace.graphql.service;

import com.typesafe.config.Config;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Value;
import org.hypertrace.graphql.config.HypertraceGraphQlServiceConfig;

@Value
class DefaultGraphQlServiceConfig implements HypertraceGraphQlServiceConfig {

  private static final String SERVICE_NAME_CONFIG = "service.name";
  private static final String SERVICE_PORT_CONFIG = "service.port";

  private static final String GRAPHQL_URL_PATH = "graphql.urlPath";
  private static final String GRAPHQL_ASYNC_SERVLET = "graphql.asyncServlet";
  private static final String GRAPHQL_CORS_ENABLED = "graphql.corsEnabled";

  private static final String DEFAULT_TENANT_ID = "defaultTenantId";

  private static final String MAX_IO_THREADS_PROPERTY = "threads.io.max";

  private static final String ATTRIBUTE_SERVICE_HOST_PROPERTY = "attribute.service.host";
  private static final String ATTRIBUTE_SERVICE_PORT_PROPERTY = "attribute.service.port";

  private static final String GATEWAY_SERVICE_HOST_PROPERTY = "gateway.service.host";
  private static final String GATEWAY_SERVICE_PORT_PROPERTY = "gateway.service.port";

  private static final String ENTITY_SERVICE_HOST_PROPERTY = "entity.service.host";
  private static final String ENTITY_SERVICE_PORT_PROPERTY = "entity.service.port";

  private static final String CONFIG_SERVICE_HOST_PROPERTY = "config.service.host";
  private static final String CONFIG_SERVICE_PORT_PROPERTY = "config.service.port";

  String serviceName;
  int servicePort;
  String graphqlUrlPath;
  boolean asyncServlet;
  boolean corsEnabled;
  Optional<String> defaultTenantId;
  int maxIoThreads;
  String attributeServiceHost;
  int attributeServicePort;
  String gatewayServiceHost;
  int gatewayServicePort;
  String entityServiceHost;
  int entityServicePort;
  String configServiceHost;
  int configServicePort;

  DefaultGraphQlServiceConfig(Config untypedConfig) {
    this.serviceName = untypedConfig.getString(SERVICE_NAME_CONFIG);
    this.servicePort = untypedConfig.getInt(SERVICE_PORT_CONFIG);
    this.graphqlUrlPath = untypedConfig.getString(GRAPHQL_URL_PATH);
    this.asyncServlet = untypedConfig.getBoolean(GRAPHQL_ASYNC_SERVLET);
    this.corsEnabled = untypedConfig.getBoolean(GRAPHQL_CORS_ENABLED);
    this.defaultTenantId = optionallyGet(() -> untypedConfig.getString(DEFAULT_TENANT_ID));
    this.maxIoThreads = untypedConfig.getInt(MAX_IO_THREADS_PROPERTY);

    this.attributeServiceHost = untypedConfig.getString(ATTRIBUTE_SERVICE_HOST_PROPERTY);
    this.attributeServicePort = untypedConfig.getInt(ATTRIBUTE_SERVICE_PORT_PROPERTY);
    this.gatewayServiceHost = untypedConfig.getString(GATEWAY_SERVICE_HOST_PROPERTY);
    this.gatewayServicePort = untypedConfig.getInt(GATEWAY_SERVICE_PORT_PROPERTY);
    this.entityServiceHost = untypedConfig.getString(ENTITY_SERVICE_HOST_PROPERTY);
    this.entityServicePort = untypedConfig.getInt(ENTITY_SERVICE_PORT_PROPERTY);
    this.configServiceHost = untypedConfig.getString(CONFIG_SERVICE_HOST_PROPERTY);
    this.configServicePort = untypedConfig.getInt(CONFIG_SERVICE_PORT_PROPERTY);
  }

  private <T> Optional<T> optionallyGet(Supplier<T> valueSupplier) {
    try {
      return Optional.ofNullable(valueSupplier.get());
    } catch (Throwable unused) {
      return Optional.empty();
    }
  }
}
