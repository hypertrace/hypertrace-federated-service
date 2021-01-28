package org.hypertrace.graphql.atttribute.scopes;

import org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;

enum HypertraceAttributeScope implements AttributeScope {
  TRACE(HypertraceCoreAttributeScopeString.TRACE),
  SPAN(HypertraceCoreAttributeScopeString.SPAN),
  API(HypertraceAttributeScopeString.API),
  API_TRACE(HypertraceAttributeScopeString.API_TRACE),
  BACKEND(HypertraceAttributeScopeString.BACKEND),
  BACKEND_TRACE(HypertraceAttributeScopeString.BACKEND_TRACE),
  INTERACTION(HypertraceAttributeScopeString.INTERACTION),
  SERVICE(HypertraceAttributeScopeString.SERVICE);

  private final String scope;

  HypertraceAttributeScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String getScopeString() {
    return this.scope;
  }
}
