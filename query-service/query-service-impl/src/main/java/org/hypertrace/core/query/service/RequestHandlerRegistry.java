package org.hypertrace.core.query.service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerConfig;

@Singleton
public class RequestHandlerRegistry {
  private final Set<RequestHandler> requestHandlers;

  @Inject
  RequestHandlerRegistry(
      QueryServiceConfig config, Set<RequestHandlerBuilder> requestHandlerInfoSet) {
    this.requestHandlers =
        config.getQueryRequestHandlersConfigs().stream()
            .map(handlerConfig -> buildFromMatchingHandler(requestHandlerInfoSet, handlerConfig))
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
  }

  public Set<RequestHandler> getAll() {
    return requestHandlers;
  }

  private RequestHandler buildFromMatchingHandler(
      Set<RequestHandlerBuilder> handlerInfoBuilders, RequestHandlerConfig config) {
    return handlerInfoBuilders.stream()
        .filter(builder -> builder.canBuild(config))
        .findFirst()
        .map(builder -> builder.build(config))
        .orElseThrow(
            () ->
                new UnsupportedOperationException(
                    "No builder registered matching provided config: " + config.toString()));
  }
}
