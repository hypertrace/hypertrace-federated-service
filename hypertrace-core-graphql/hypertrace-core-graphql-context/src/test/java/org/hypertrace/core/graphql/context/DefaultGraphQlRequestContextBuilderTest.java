package org.hypertrace.core.graphql.context;

import static org.hypertrace.core.graphql.context.DefaultGraphQlRequestContextBuilder.AUTHORIZATION_HEADER_KEY;
import static org.hypertrace.core.graphql.context.DefaultGraphQlRequestContextBuilder.TENANT_ID_HEADER_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Injector;
import graphql.schema.DataFetcher;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultGraphQlRequestContextBuilderTest {

  @Mock Injector mockInjector;
  @Mock HttpServletRequest mockRequest;
  @Mock HttpServletResponse mockResponse;
  @Mock GraphQlServiceConfig mockServiceConfig;

  GraphQlRequestContextBuilder contextBuilder;
  GraphQlRequestContext requestContext;

  @BeforeEach
  void beforeEach() {
    this.contextBuilder =
        new DefaultGraphQlRequestContextBuilder(this.mockInjector, this.mockServiceConfig);
    this.requestContext = this.contextBuilder.build(this.mockRequest, this.mockResponse);
  }

  @Test
  void returnsAuthorizationHeaderIfPresent() {
    when(this.mockRequest.getHeader(eq(AUTHORIZATION_HEADER_KEY))).thenReturn("Bearer ABC");
    when(this.mockRequest.getHeader(eq(AUTHORIZATION_HEADER_KEY.toLowerCase())))
        .thenReturn("Bearer abc");
    assertEquals(Optional.of("Bearer ABC"), this.requestContext.getAuthorizationHeader());

    when(this.mockRequest.getHeader(eq(AUTHORIZATION_HEADER_KEY))).thenReturn(null);
    assertEquals(Optional.of("Bearer abc"), this.requestContext.getAuthorizationHeader());
  }

  @Test
  void returnsEmptyOptionalIfNoAuthorizationHeaderPresent() {
    when(this.mockRequest.getHeader(any())).thenReturn(null);
    assertEquals(Optional.empty(), this.requestContext.getAuthorizationHeader());
  }

  @Test
  void delegatesDataLoaderRegistry() {
    assertTrue(this.requestContext.getDataLoaderRegistry().isPresent());
  }

  @Test
  void canConstructDataFetcher() {
    this.requestContext.constructDataFetcher(DataFetcher.class);
    verify(this.mockInjector).getInstance(DataFetcher.class);
  }

  @Test
  void returnsTenantIdIfTenantIdHeaderPresent() {
    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("test tenant id");
    assertEquals(Optional.of("test tenant id"), this.requestContext.getTenantId());
  }

  @Test
  void returnsDefaultTenantIdOnlyIfNoHeaderPresent() {
    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("test tenant id");
    when(this.mockServiceConfig.getDefaultTenantId()).thenReturn(Optional.of("default tenant id"));
    assertEquals(Optional.of("test tenant id"), this.requestContext.getTenantId());
    reset(this.mockRequest);
    assertEquals(Optional.of("default tenant id"), this.requestContext.getTenantId());
  }

  @Test
  void returnsCachingKeyForNoAuth() {
    assertNotNull(this.requestContext.getCachingKey());
  }

  @Test
  void returnsCachingKeysEqualForSameTenant() {
    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("first tenant id");
    var firstKey = this.contextBuilder.build(this.mockRequest, this.mockResponse).getCachingKey();
    var secondKey = this.contextBuilder.build(this.mockRequest, this.mockResponse).getCachingKey();
    assertEquals(firstKey, secondKey);
    assertNotSame(firstKey, secondKey);

    when(this.mockRequest.getHeader(TENANT_ID_HEADER_KEY)).thenReturn("second tenant id");
    var thirdKey = this.contextBuilder.build(this.mockRequest, this.mockResponse).getCachingKey();
    assertNotEquals(firstKey, thirdKey);
  }
}
