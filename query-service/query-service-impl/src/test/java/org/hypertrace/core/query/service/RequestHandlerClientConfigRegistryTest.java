package org.hypertrace.core.query.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.typesafe.config.ConfigFactory;
import java.util.Objects;
import org.hypertrace.core.query.service.pinot.QueryRequestToPinotSQLConverterTest;
import org.junit.jupiter.api.Test;

class RequestHandlerClientConfigRegistryTest {
  private final QueryServiceConfig config =
      new QueryServiceConfig(
          ConfigFactory.parseURL(
                  Objects.requireNonNull(
                      QueryRequestToPinotSQLConverterTest.class
                          .getClassLoader()
                          .getResource("application.conf")))
              .getConfig("service.config"));

  @Test
  void returnsClientConfigForMatch() {
    assertTrue(new RequestHandlerClientConfigRegistry(config).get("broker").isPresent());
    assertFalse(new RequestHandlerClientConfigRegistry(config).get("non-existent").isPresent());
  }
}
