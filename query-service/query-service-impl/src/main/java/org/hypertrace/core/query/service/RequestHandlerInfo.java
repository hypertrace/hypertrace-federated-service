package org.hypertrace.core.query.service;

import com.typesafe.config.Config;

public class RequestHandlerInfo {

  private final String name;

  private final Class<? extends RequestHandler> requestHandlerClazz;

  private final Config config;

  public RequestHandlerInfo(
      String name,
      Class<? extends RequestHandler> requestHandlerClazz,
      Config config) {
    this.name = name;
    this.requestHandlerClazz = requestHandlerClazz;
    this.config = config;
  }

  public String getName() {
    return name;
  }

  public Class<? extends RequestHandler> getRequestHandlerClazz() {
    return requestHandlerClazz;
  }

  public Config getConfig() {
    return config;
  }
}
