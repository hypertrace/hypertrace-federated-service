package org.hypertrace.core.query.service.pinot;

import java.util.Collections;
import java.util.Map;
import org.apache.pinot.client.ResultSetGroup;
import org.hypertrace.core.query.service.pinot.PinotClientFactory.PinotClient;

/*
 * PinotQuery provides basic interface for getting query response from Pinot.
 */
public abstract class PinotQuery<T> {

  private final String name;
  private final PinotClient pinotClient;

  public PinotQuery(String name, PinotClient pinotClient) {
    this.name = name;
    this.pinotClient = pinotClient;
  }

  public String getName() {
    return this.name;
  }

  abstract String getQuery(Map<String, Object> args);

  abstract T convertQueryResults(ResultSetGroup queryResults);

  public T execute() {
    return execute(Collections.emptyMap());
  }

  public T execute(Map<String, Object> args) {
    final ResultSetGroup queryResults =
        this.pinotClient.executeQuery(getQuery(args), Params.newBuilder().build());
    return convertQueryResults(queryResults);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + (name == null ? 0 : name.hashCode());
    hash = 31 * hash + (pinotClient == null ? 0 : pinotClient.hashCode());
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (this.getClass() != o.getClass()) {
      return false;
    }
    PinotQuery pq = (PinotQuery) o;
    return (this.name.equals(pq.name) && this.pinotClient == pq.pinotClient);
  }
}
