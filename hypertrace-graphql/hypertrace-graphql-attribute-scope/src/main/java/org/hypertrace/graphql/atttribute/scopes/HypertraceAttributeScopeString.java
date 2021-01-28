package org.hypertrace.graphql.atttribute.scopes;

import org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString;

public interface HypertraceAttributeScopeString extends HypertraceCoreAttributeScopeString {
  String API = "API";
  String API_TRACE = "API_TRACE";
  String BACKEND = "BACKEND";
  String BACKEND_TRACE = "BACKEND_TRACE";
  String INTERACTION = "INTERACTION";
  String SERVICE = "SERVICE";
}
