package org.hypertrace.core.query.service;

import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerConfig;

public interface RequestHandlerBuilder {

  boolean canBuild(RequestHandlerConfig config);

  RequestHandler build(RequestHandlerConfig config);
}
