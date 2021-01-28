package org.hypertrace.core.query.service;

import java.util.Optional;
import javax.inject.Inject;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandlerSelector {

  private static final Logger LOG = LoggerFactory.getLogger(RequestHandlerSelector.class);

  private final RequestHandlerRegistry registry;

  @Inject
  public RequestHandlerSelector(RequestHandlerRegistry registry) {
    this.registry = registry;
  }

  public Optional<RequestHandler> select(QueryRequest request, ExecutionContext executionContext) {

    // check if each of the requestHandler can handle the request and return the cost of serving
    // that query
    double minCost = Double.MAX_VALUE;
    RequestHandler selectedHandler = null;
    for (RequestHandler requestHandler : registry.getAll()) {
      QueryCost queryCost = requestHandler.canHandle(request, executionContext);
      double cost = queryCost.getCost();
      if (LOG.isDebugEnabled()) {
        LOG.debug("Request handler: {}, query cost: {}", requestHandler.getName(), cost);
      }
      if (cost >= 0 && cost < minCost) {
        minCost = cost;
        selectedHandler = requestHandler;
      }
    }

    if (selectedHandler != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "Selected requestHandler: {} for the query: {}; referencedColumns: {}, cost: {}",
            selectedHandler.getName(),
            request,
            executionContext.getReferencedColumns(),
            minCost);
      }
    } else {
      LOG.error(
          "No requestHandler for the query: {}; referencedColumns: {}, cost: {}",
          request,
          executionContext.getReferencedColumns(),
          minCost);
    }
    return Optional.ofNullable(selectedHandler);
  }
}
