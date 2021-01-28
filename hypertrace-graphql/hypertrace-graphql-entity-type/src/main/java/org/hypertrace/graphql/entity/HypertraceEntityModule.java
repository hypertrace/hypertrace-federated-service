package org.hypertrace.graphql.entity;

import com.google.inject.AbstractModule;
import org.hypertrace.graphql.entity.type.HypertraceEntityTypeModule;

public class HypertraceEntityModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new HypertraceEntityTypeModule());
  }
}
