package org.hypertrace.graphql.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import com.google.inject.Guice;
import org.hypertrace.core.graphql.spi.lifecycle.GraphQlServiceLifecycle;
import org.hypertrace.graphql.config.HypertraceGraphQlServiceConfig;
import org.junit.jupiter.api.Test;

public class GraphQlModuleTest {

  @Test
  public void testResolveBindings() {
    assertDoesNotThrow(
        () ->
            Guice.createInjector(
                    new GraphQlModule(
                        mock(HypertraceGraphQlServiceConfig.class),
                        mock(GraphQlServiceLifecycle.class)))
                .getAllBindings());
  }
}
