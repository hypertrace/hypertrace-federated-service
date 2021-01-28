package org.hypertrace.graphql.atttribute.scopes;

import static org.hypertrace.core.graphql.attributes.IdMapping.forForeignId;
import static org.hypertrace.core.graphql.attributes.IdMapping.forId;
import static org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString.SPAN;
import static org.hypertrace.core.graphql.atttributes.scopes.HypertraceCoreAttributeScopeString.TRACE;
import static org.hypertrace.graphql.atttribute.scopes.HypertraceAttributeScopeString.API;
import static org.hypertrace.graphql.atttribute.scopes.HypertraceAttributeScopeString.API_TRACE;
import static org.hypertrace.graphql.atttribute.scopes.HypertraceAttributeScopeString.BACKEND;
import static org.hypertrace.graphql.atttribute.scopes.HypertraceAttributeScopeString.BACKEND_TRACE;
import static org.hypertrace.graphql.atttribute.scopes.HypertraceAttributeScopeString.SERVICE;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.graphql.attributes.IdMapping;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeScope;

public class HypertraceAttributeScopeModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Key.get(new TypeLiteral<Class<? extends AttributeScope>>() {}))
        .toInstance(HypertraceAttributeScope.class);

    Multibinder<IdMapping> idBinder = Multibinder.newSetBinder(binder(), IdMapping.class);
    idBinder.addBinding().toInstance(forId(SPAN, "id"));
    idBinder.addBinding().toInstance(forForeignId(SPAN, TRACE, "traceId"));
    idBinder.addBinding().toInstance(forForeignId(SPAN, API_TRACE, "apiTraceId"));

    idBinder.addBinding().toInstance(forId(TRACE, "id"));

    idBinder.addBinding().toInstance(forId(API, "id"));
    idBinder.addBinding().toInstance(forForeignId(API, SERVICE, "serviceId"));

    idBinder.addBinding().toInstance(forId(SERVICE, "id"));

    idBinder.addBinding().toInstance(forId(BACKEND, "id"));

    idBinder.addBinding().toInstance(forId(API_TRACE, "apiTraceId"));
    idBinder.addBinding().toInstance(forForeignId(API_TRACE, SERVICE, "serviceId"));
    idBinder.addBinding().toInstance(forForeignId(API_TRACE, API, "apiId"));
    idBinder.addBinding().toInstance(forForeignId(API_TRACE, TRACE, "traceId"));

    idBinder.addBinding().toInstance(forId(BACKEND_TRACE, "backendTraceId"));
    idBinder.addBinding().toInstance(forForeignId(BACKEND_TRACE, SERVICE, "callerServiceId"));
    idBinder.addBinding().toInstance(forForeignId(BACKEND_TRACE, API, "callerApiId"));
    idBinder.addBinding().toInstance(forForeignId(BACKEND_TRACE, TRACE, "traceId"));
    idBinder.addBinding().toInstance(forForeignId(BACKEND_TRACE, BACKEND, "backendId"));
  }
}
