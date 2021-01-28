package org.hypertrace.core.query.service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerClientConfig;

public class RequestHandlerClientConfigRegistry {
  private final Map<String, RequestHandlerClientConfig> clientConfigMap;

  @Inject
  RequestHandlerClientConfigRegistry(QueryServiceConfig queryServiceConfig) {
    this.clientConfigMap =
        queryServiceConfig.getRequestHandlerClientConfigs().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    RequestHandlerClientConfig::getType, Function.identity()));
  }

  public Optional<RequestHandlerClientConfig> get(String key) {
    return Optional.ofNullable(this.clientConfigMap.get(key));
  }
}
