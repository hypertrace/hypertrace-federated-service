package org.hypertrace.graphql.entity.type;

import org.hypertrace.graphql.atttribute.scopes.HypertraceAttributeScopeString;
import org.hypertrace.graphql.entity.schema.EntityType;

enum HypertraceEntityType implements EntityType {
  API(HypertraceAttributeScopeString.API),
  SERVICE(HypertraceAttributeScopeString.SERVICE),
  BACKEND(HypertraceAttributeScopeString.BACKEND);

  private final String scope;

  HypertraceEntityType(String scope) {
    this.scope = scope;
  }

  @Override
  public String getScopeString() {
    return this.scope;
  }
}
