package org.hypertrace.core.query.service;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestHandlerRegistryTest {

  @Mock QueryServiceConfig mockConfig;
  @Mock RequestHandlerBuilder mockBuilder;
  @Mock RequestHandlerConfig mockHandlerConfig;

  @BeforeEach
  void beforeEach() {
    when(this.mockConfig.getQueryRequestHandlersConfigs())
        .thenReturn(List.of(this.mockHandlerConfig));
  }

  @Test
  void buildsHandlerIfBuilderFound() {
    RequestHandler mockHandler = mock(RequestHandler.class);
    when(this.mockBuilder.canBuild(any(RequestHandlerConfig.class))).thenReturn(true);
    when(this.mockBuilder.build(any(RequestHandlerConfig.class))).thenReturn(mockHandler);

    assertIterableEquals(
        Set.of(mockHandler),
        new RequestHandlerRegistry(this.mockConfig, Set.of(this.mockBuilder)).getAll());
  }

  @Test
  void testThrowsForUnsupportedHandler() {
    when(this.mockBuilder.canBuild(any(RequestHandlerConfig.class))).thenReturn(false);

    assertThrows(
        UnsupportedOperationException.class,
        () -> new RequestHandlerRegistry(mockConfig, Set.of(mockBuilder)));
  }
}
