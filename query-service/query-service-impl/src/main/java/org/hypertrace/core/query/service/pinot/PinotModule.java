package org.hypertrace.core.query.service.pinot;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.query.service.RequestHandlerClientConfigRegistry;
import org.hypertrace.core.query.service.RequestHandlerBuilder;

public class PinotModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), RequestHandlerBuilder.class)
        .addBinding()
        .to(PinotRequestHandlerBuilder.class);
    requireBinding(RequestHandlerClientConfigRegistry.class);
  }
}
