package org.hypertrace.core.query.service.pinot;

import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.pinot.client.ResultSetGroup;

/*
 * AdhocPinotQuery could take any Pinot query and return ResultSetGroup which is the raw Pinot
 * Response.
 */
@NotThreadSafe
public class AdhocPinotQuery extends PinotQuery<ResultSetGroup> {

  private String query;

  public AdhocPinotQuery(String name, PinotClientFactory.PinotClient pinotClient) {
    super(name, pinotClient);
  }

  @Override
  public String getQuery(Map<String, Object> args) {
    return this.query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  ResultSetGroup convertQueryResults(ResultSetGroup queryResults) {
    return queryResults;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 31 * hash + (query == null ? 0 : query.hashCode());
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (super.equals(o)) {
      if (this.getClass() != o.getClass()) {
        return false;
      }
      AdhocPinotQuery apq = (AdhocPinotQuery) o;
      return (this.query.equals(apq.query));
    }
    return false;
  }
}
