package org.hypertrace.core.query.service;

import io.reactivex.rxjava3.core.Observable;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.Row;

/**
 * Interface to be implemented by the different handlers which will handle the queries coming into
 * the query service. We expect different implementations for different data stores that could have
 * data which is served by the query service.
 *
 * <p>The callers of this will be first checking if the handler can handle the given query, and can
 * decide whether they want this handler to handle the query.
 *
 * <p>QueryCost cost = handler.canHandle(....);
 *
 * <p>// If the caller decides to use this handler handler.handleRequest(....);
 */
public interface RequestHandler {

  String getName();

  QueryCost canHandle(QueryRequest request, ExecutionContext context);

  /** Handle the request and add rows to the collector. */
  Observable<Row> handleRequest(QueryRequest request, ExecutionContext executionContext);
}
