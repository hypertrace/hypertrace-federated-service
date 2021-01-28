package org.hypertrace.core.query.service.pinot;

import javax.inject.Inject;
import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerClientConfig;
import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerConfig;
import org.hypertrace.core.query.service.RequestHandlerClientConfigRegistry;
import org.hypertrace.core.query.service.RequestHandler;
import org.hypertrace.core.query.service.RequestHandlerBuilder;

public class PinotRequestHandlerBuilder implements RequestHandlerBuilder {

  private final RequestHandlerClientConfigRegistry clientConfigRegistry;

  @Inject
  PinotRequestHandlerBuilder(RequestHandlerClientConfigRegistry clientConfigRegistry) {
    this.clientConfigRegistry = clientConfigRegistry;
  }

  @Override
  public boolean canBuild(RequestHandlerConfig config) {
    return "pinot".equals(config.getType());
  }

  @Override
  public RequestHandler build(RequestHandlerConfig config) {

    RequestHandlerClientConfig clientConfig =
        this.clientConfigRegistry
            .get(config.getClientConfig())
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Client config requested but not registered: " + config.getClientConfig()));

    PinotClientFactory.createPinotClient(
        config.getName(), clientConfig.getType(), clientConfig.getConnectionString());

    return new PinotBasedRequestHandler(config.getName(), config.getRequestHandlerInfo());
  }
}
