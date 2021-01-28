package org.hypertrace.core.query.service.projection;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.hypertrace.core.query.service.QueryTransformation;

public class ProjectionModule extends AbstractModule {

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), QueryTransformation.class)
        .addBinding()
        .to(ProjectionTransformation.class);
  }
}
