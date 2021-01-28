package org.hypertrace.graphql.explorer.context;

import org.hypertrace.graphql.atttribute.scopes.HypertraceAttributeScopeString;
import org.hypertrace.graphql.explorer.schema.argument.ExplorerContext;

enum HypertraceExplorerContext implements ExplorerContext {
  TRACE(HypertraceAttributeScopeString.TRACE),
  API(HypertraceAttributeScopeString.API),
  SERVICE(HypertraceAttributeScopeString.SERVICE),
  BACKEND(HypertraceAttributeScopeString.BACKEND),
  API_TRACE(HypertraceAttributeScopeString.API_TRACE),
  BACKEND_TRACE(HypertraceAttributeScopeString.BACKEND_TRACE),
  SPAN(HypertraceAttributeScopeString.SPAN);

  private final String scope;

  HypertraceExplorerContext(String scope) {
    this.scope = scope;
  }

  @Override
  public String getScopeString() {
    return this.scope;
  }
}
