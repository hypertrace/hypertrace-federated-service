package org.hypertrace.core.query.service;

import com.typesafe.config.Config;
import java.util.List;
import java.util.stream.Collectors;

public class QueryServiceConfig {

  private static final String CONFIG_PATH_HANDLER_CLIENT_LIST = "clients";
  private static final String CONFIG_PATH_HANDLER_CONFIG_LIST = "queryRequestHandlersConfig";
  private static final String CONFIG_PATH_ATTRIBUTE_CLIENT = "attribute.client";

  private final List<RequestHandlerClientConfig> requestHandlerClientConfigs;
  private final List<RequestHandlerConfig> queryRequestHandlersConfigs;
  private final ClientHostPortConfig attributeClientConfig;

  QueryServiceConfig(Config config) {
    Config resolved = config.resolve();
    this.attributeClientConfig =
        new ClientHostPortConfig(resolved.getConfig(CONFIG_PATH_ATTRIBUTE_CLIENT));
    this.requestHandlerClientConfigs =
        resolved.getConfigList(CONFIG_PATH_HANDLER_CLIENT_LIST).stream()
            .map(RequestHandlerClientConfig::new)
            .collect(Collectors.toUnmodifiableList());
    this.queryRequestHandlersConfigs =
        resolved.getConfigList(CONFIG_PATH_HANDLER_CONFIG_LIST).stream()
            .map(RequestHandlerConfig::new)
            .collect(Collectors.toUnmodifiableList());
  }

  public List<RequestHandlerClientConfig> getRequestHandlerClientConfigs() {
    return this.requestHandlerClientConfigs;
  }

  public List<RequestHandlerConfig> getQueryRequestHandlersConfigs() {
    return this.queryRequestHandlersConfigs;
  }

  public ClientHostPortConfig getAttributeClientConfig() {
    return this.attributeClientConfig;
  }

  public static class RequestHandlerConfig {
    private static final String CONFIG_PATH_NAME = "name";
    private static final String CONFIG_PATH_TYPE = "type";
    private static final String CONFIG_PATH_CLIENT_KEY = "clientConfig";
    private static final String CONFIG_PATH_REQUEST_HANDLER_INFO = "requestHandlerInfo";

    private final String name;
    private final String type;
    private final String clientConfig;
    private final Config requestHandlerInfo;

    private RequestHandlerConfig(Config config) {
      this.name = config.getString(CONFIG_PATH_NAME);
      this.type = config.getString(CONFIG_PATH_TYPE);
      this.clientConfig = config.getString(CONFIG_PATH_CLIENT_KEY);
      this.requestHandlerInfo = config.getConfig(CONFIG_PATH_REQUEST_HANDLER_INFO);
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public String getClientConfig() {
      return clientConfig;
    }

    public Config getRequestHandlerInfo() {
      return requestHandlerInfo;
    }
  }

  public static class RequestHandlerClientConfig {
    private static final String CONFIG_PATH_TYPE = "type";
    private static final String CONFIG_PATH_CONNECTION_STRING = "connectionString";
    private String type;
    private String connectionString;

    private RequestHandlerClientConfig(Config config) {
      this.type = config.getString(CONFIG_PATH_TYPE);
      this.connectionString = config.getString(CONFIG_PATH_CONNECTION_STRING);
    }

    public String getType() {
      return type;
    }

    public String getConnectionString() {
      return connectionString;
    }
  }

  public static class ClientHostPortConfig {
    private static final String CONFIG_PATH_HOST = "host";
    private static final String CONFIG_PATH_PORT = "port";
    private String host;
    private int port;

    private ClientHostPortConfig(Config config) {
      this.host = config.getString(CONFIG_PATH_HOST);
      this.port = config.getInt(CONFIG_PATH_PORT);
    }

    public String getHost() {
      return this.host;
    }

    public int getPort() {
      return this.port;
    }
  }
}
