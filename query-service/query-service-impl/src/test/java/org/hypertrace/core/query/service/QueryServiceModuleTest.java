package org.hypertrace.core.query.service;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import org.hypertrace.core.serviceframework.spi.PlatformServiceLifecycle;
import org.junit.jupiter.api.Test;

class QueryServiceModuleTest {
  @Test
  public void testResolveBindings() {
    Config config =
        ConfigFactory.parseFile(
                new File(
                    requireNonNull(
                            QueryServiceConfigTest.class
                                .getClassLoader()
                                .getResource("application.conf"))
                        .getPath()))
            .getConfig("service.config");
    assertDoesNotThrow(
        () ->
            Guice.createInjector(
                    new QueryServiceModule(config, mock(PlatformServiceLifecycle.class)))
                .getAllBindings());
  }
}
