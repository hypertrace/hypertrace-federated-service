package org.hypertrace.core.attribute.service.cachingclient;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.reactivex.rxjava3.core.Single;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeMetadataFilter;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.attribute.service.v1.AttributeServiceGrpc.AttributeServiceImplBase;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachingAttributeClientTest {

  AttributeMetadata metadata1 =
      AttributeMetadata.newBuilder()
          .setScopeString(AttributeScope.EVENT.name())
          .setKey("first")
          .setId("first-id")
          .build();
  AttributeMetadata metadata2 =
      AttributeMetadata.newBuilder()
          .setScopeString(AttributeScope.EVENT.name())
          .setKey("second")
          .setId("second-id")
          .build();

  @Mock RequestContext mockContext;

  @Mock AttributeServiceImplBase mockAttributeService;

  CachingAttributeClient attributeClient;

  Server grpcServer;
  ManagedChannel grpcChannel;
  Context grpcTestContext;
  List<AttributeMetadata> responseMetadata;
  Optional<Throwable> responseError;

  @BeforeEach
  void beforeEach() throws IOException {
    String uniqueName = InProcessServerBuilder.generateName();
    this.grpcServer =
        InProcessServerBuilder.forName(uniqueName)
            .directExecutor() // directExecutor is fine for unit tests
            .addService(mockAttributeService)
            .build()
            .start();
    this.grpcChannel = InProcessChannelBuilder.forName(uniqueName).directExecutor().build();
    this.attributeClient = CachingAttributeClient.builder(this.grpcChannel).build();
    when(this.mockContext.getTenantId()).thenReturn(Optional.of("default tenant"));
    this.grpcTestContext = Context.current().withValue(RequestContext.CURRENT, this.mockContext);
    this.responseMetadata = List.of(this.metadata1, this.metadata2);
    this.responseError = Optional.empty();
    doAnswer(
            invocation -> {
              StreamObserver<AttributeMetadata> observer =
                  invocation.getArgument(1, StreamObserver.class);
              responseError.ifPresentOrElse(
                  observer::onError,
                  () -> {
                    this.responseMetadata.forEach(observer::onNext);
                    observer.onCompleted();
                  });
              return null;
            })
        .when(this.mockAttributeService)
        .findAttributes(any(), any());
  }

  @AfterEach
  void afterEach() {
    this.grpcServer.shutdownNow();
    this.grpcChannel.shutdownNow();
  }

  @Test
  void cachesConsecutiveGetAllCallsInSameContext() throws Exception {
    assertSame(
        this.metadata1,
        this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet()));
    verify(this.mockAttributeService, times(1)).findAttributes(any(), any());
    verifyNoMoreInteractions(this.mockAttributeService);
    assertSame(
        this.metadata2,
        this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "second").blockingGet()));
  }

  @Test
  void throwsErrorIfNoKeyMatch() {
    assertThrows(
        NoSuchElementException.class,
        () ->
            this.grpcTestContext.run(
                () -> this.attributeClient.get("EVENT", "fake").blockingGet()));
  }

  @Test
  void lazilyFetchesOnSubscribe() throws Exception {
    Single<AttributeMetadata> attribute =
        this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first"));
    verifyNoInteractions(this.mockAttributeService);
    attribute.subscribe();
    verify(this.mockAttributeService, times(1)).findAttributes(any(), any());
  }

  @Test
  void supportsMultipleConcurrentCacheKeys() throws Exception {
    AttributeMetadata defaultRetrieved =
        this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet());
    assertSame(this.metadata1, defaultRetrieved);
    verify(this.mockAttributeService, times(1)).findAttributes(any(), any());

    RequestContext otherMockContext = mock(RequestContext.class);
    when(otherMockContext.getTenantId()).thenReturn(Optional.of("other tenant"));
    Context otherGrpcContext =
        Context.current().withValue(RequestContext.CURRENT, otherMockContext);
    AttributeMetadata otherContextMetadata = AttributeMetadata.newBuilder(this.metadata1).build();

    this.responseMetadata = List.of(otherContextMetadata);

    AttributeMetadata otherRetrieved =
        otherGrpcContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet());
    assertSame(otherContextMetadata, otherRetrieved);
    assertNotSame(defaultRetrieved, otherRetrieved);
    verify(this.mockAttributeService, times(2)).findAttributes(any(), any());
    verifyNoMoreInteractions(this.mockAttributeService);

    assertSame(
        defaultRetrieved,
        this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet()));

    assertSame(
        otherRetrieved,
        otherGrpcContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet()));
  }

  @Test
  void retriesOnError() throws Exception {
    this.responseError = Optional.of(new UnsupportedOperationException());

    assertThrows(
        StatusRuntimeException.class,
        () ->
            this.grpcTestContext.call(
                () -> this.attributeClient.get("EVENT", "first").blockingGet()));
    verify(this.mockAttributeService, times(1)).findAttributes(any(), any());

    this.responseError = Optional.empty();
    assertSame(
        this.metadata1,
        this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet()));
    verify(this.mockAttributeService, times(2)).findAttributes(any(), any());
  }

  @Test
  void hasConfigurableCacheSize() throws Exception {
    this.attributeClient =
        CachingAttributeClient.builder(this.grpcChannel).withMaximumCacheContexts(1).build();

    RequestContext otherMockContext = mock(RequestContext.class);
    when(otherMockContext.getTenantId()).thenReturn(Optional.of("other tenant"));
    this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet());

    // This call should evict the original call
    Context.current()
        .withValue(RequestContext.CURRENT, otherMockContext)
        .call(() -> this.attributeClient.get("EVENT", "first").blockingGet());

    // Rerunning this call now fire again, a third server call
    this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet());
    verify(this.mockAttributeService, times(3)).findAttributes(any(), any());
  }

  @Test
  void supportsAppliedFilter() throws Exception {
    AttributeMetadataFilter attributeMetadataFilter =
        AttributeMetadataFilter.newBuilder().addScope(AttributeScope.EVENT).build();
    this.attributeClient =
        CachingAttributeClient.builder(this.grpcChannel)
            .withAttributeFilter(attributeMetadataFilter)
            .build();
    this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet());
    verify(this.mockAttributeService, times(1)).findAttributes(eq(attributeMetadataFilter), any());
  }

  @Test
  void supportsCachedLookupById() throws Exception {
    assertSame(
        this.metadata1,
        this.grpcTestContext.call(() -> this.attributeClient.get("first-id").blockingGet()));
    verify(this.mockAttributeService, times(1)).findAttributes(any(), any());
    verifyNoMoreInteractions(this.mockAttributeService);
    assertSame(
        this.metadata2,
        this.grpcTestContext.call(() -> this.attributeClient.get("second-id").blockingGet()));
  }

  @Test
  void sharesIdAndKeyCache() throws Exception {
    assertSame(
        this.metadata1,
        this.grpcTestContext.call(() -> this.attributeClient.get("first-id").blockingGet()));
    verify(this.mockAttributeService, times(1)).findAttributes(any(), any());
    verifyNoMoreInteractions(this.mockAttributeService);
    assertSame(
        this.metadata1,
        this.grpcTestContext.call(() -> this.attributeClient.get("EVENT", "first").blockingGet()));
  }

  @Test
  void throwsErrorIfNoIdMatch() {
    assertThrows(
        NoSuchElementException.class,
        () -> this.grpcTestContext.run(() -> this.attributeClient.get("fakeId").blockingGet()));
  }

  @Test
  void getsAllAttributesInScope() throws Exception {
    assertEquals(
        this.responseMetadata,
        this.grpcTestContext.call(() -> this.attributeClient.getAllInScope("EVENT").blockingGet()));

    assertEquals(
        emptyList(),
        this.grpcTestContext.call(
            () -> this.attributeClient.getAllInScope("DOESNT_EXIST").blockingGet()));
  }
}
