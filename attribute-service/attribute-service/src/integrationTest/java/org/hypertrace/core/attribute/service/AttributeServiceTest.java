package org.hypertrace.core.attribute.service;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.client.AttributeServiceClient;
import org.hypertrace.core.attribute.service.v1.AttributeCreateRequest;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeMetadataFilter;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.attribute.service.v1.AttributeSource;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadataDeleteRequest;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadataUpdateRequest;
import org.hypertrace.core.attribute.service.v1.AttributeType;
import org.hypertrace.core.serviceframework.IntegrationTestServerUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Integration test for AttributeService */
public class AttributeServiceTest {
  private static final String SPAN_ID_ATTR = "EVENT.id";
  private static final String SPAN_NAME_ATTR = "EVENT.name";
  private static final String TRACE_METRIC_ATTR = "TRACE.duration";

  private static final String TEST_TENANT_ID = "test-tenant-id";

  private final AttributeMetadata spanNameAttr =
      AttributeMetadata.newBuilder()
          .setId(SPAN_NAME_ATTR)
          .setFqn(SPAN_NAME_ATTR)
          .setScopeString(AttributeScope.EVENT.name())
          .setKey("name")
          .setType(AttributeType.ATTRIBUTE)
          .setValueKind(AttributeKind.TYPE_STRING)
          .setDisplayName("Span Name")
          .addSources(AttributeSource.QS)
          .build();
  private final AttributeMetadata spanIdAttr =
      AttributeMetadata.newBuilder()
          .setId(SPAN_ID_ATTR)
          .setFqn(SPAN_ID_ATTR)
          .setScopeString(AttributeScope.EVENT.name())
          .setKey("id")
          .setType(AttributeType.ATTRIBUTE)
          .setValueKind(AttributeKind.TYPE_STRING)
          .setDisplayName("Span Id")
          .addSources(AttributeSource.QS)
          .build();

  private final AttributeMetadata traceDurationMillis =
      AttributeMetadata.newBuilder()
          .setId(TRACE_METRIC_ATTR)
          .setFqn(TRACE_METRIC_ATTR)
          .setScopeString(AttributeScope.TRACE.name())
          .setKey("duration")
          .setType(AttributeType.METRIC)
          .setValueKind(AttributeKind.TYPE_INT64)
          .setDisplayName("Duration")
          .addSources(AttributeSource.QS)
          .build();

  private final Map<String, String> requestHeaders = Map.of("x-tenant-id", TEST_TENANT_ID);

  private static AttributeServiceClient client;

  @BeforeAll
  public static void setup() {
    System.out.println("Testing the Attribute E2E Test");
    IntegrationTestServerUtil.startServices(new String[] {"attribute-service"});
    Channel channel = ManagedChannelBuilder.forAddress("localhost", 9012).usePlaintext().build();
    client = new AttributeServiceClient(channel);
  }

  @AfterAll
  public static void teardown() {
    IntegrationTestServerUtil.shutdownServices();
  }

  @BeforeEach
  public void setupMethod() {
    client.delete(requestHeaders, AttributeMetadataFilter.getDefaultInstance());
  }

  @Test
  public void testCreateInvalidUseHeaders() {
    Assertions.assertThrows(RuntimeException.class, () -> testCreateInvalid(true));
  }

  @Test
  public void testCreateInvalidUseTenantId() {
    Assertions.assertThrows(RuntimeException.class, () -> testCreateInvalid(false));
  }

  private void testCreateInvalid(boolean useRequestHeaders) {
    AttributeCreateRequest attributeCreateRequest =
        AttributeCreateRequest.newBuilder()
            .addAttributes(
                AttributeMetadata.newBuilder()
                    .setId(SPAN_NAME_ATTR)
                    .setFqn(SPAN_NAME_ATTR)
                    .setKey("name")
                    .setType(AttributeType.ATTRIBUTE)
                    .setValueKind(AttributeKind.TYPE_STRING)
                    .setDisplayName("Span Name")
                    .addSources(AttributeSource.QS)
                    .build())
            .build();
    if (useRequestHeaders) {
      client.create(requestHeaders, attributeCreateRequest);
    } else {
      client.create(TEST_TENANT_ID, attributeCreateRequest);
    }
  }

  @Test
  public void testCreateCallWithHeadersWithTenantIdHeader() {
    AttributeCreateRequest attributeCreateRequest =
        AttributeCreateRequest.newBuilder()
            .addAttributes(spanNameAttr)
            .addAttributes(spanIdAttr)
            .build();
    client.create(requestHeaders, attributeCreateRequest);

    List<String> attributeMetadataList =
        Streams.stream(
                client.findAttributes(requestHeaders, AttributeMetadataFilter.getDefaultInstance()))
            .map(AttributeMetadata::getId)
            .sorted()
            .collect(Collectors.toList());
    assertEquals(attributeMetadataList, List.of(spanIdAttr.getId(), spanNameAttr.getId()));
  }

  @Test
  public void testCreateCallWithHeadersWithNoTenantIdHeader_shouldThrowException() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> {
          Map<String, String> headers = Map.of("a1", "v1");
          AttributeCreateRequest attributeCreateRequest =
              AttributeCreateRequest.newBuilder()
                  .addAttributes(spanNameAttr)
                  .addAttributes(spanIdAttr)
                  .build();
          client.create(headers, attributeCreateRequest);
        });
  }

  @Test
  public void testUpdateAndDeleteSourceMetadataForAttributeUseHeaders() {
    testUpdateAndDeleteSourceMetadataForAttribute(true);
  }

  @Test
  public void testUpdateAndDeleteSourceMetadataForAttributeUseTenantId() {
    testUpdateAndDeleteSourceMetadataForAttribute(false);
  }

  private void testUpdateAndDeleteSourceMetadataForAttribute(boolean useRequestHeaders) {
    AttributeCreateRequest attributeCreateRequest =
        AttributeCreateRequest.newBuilder()
            .addAttributes(spanNameAttr)
            .addAttributes(spanIdAttr)
            .build();
    if (useRequestHeaders) {
      client.create(requestHeaders, attributeCreateRequest);
    } else {
      client.create(TEST_TENANT_ID, attributeCreateRequest);
    }

    AttributeSourceMetadataUpdateRequest request =
        AttributeSourceMetadataUpdateRequest.newBuilder()
            .setFqn(SPAN_NAME_ATTR)
            .setSource(AttributeSource.EDS)
            .putAllSourceMetadata(Map.of(SPAN_NAME_ATTR, "attributes.SPAN_NAME"))
            .build();

    List<AttributeMetadata> attributeMetadataList;
    if (useRequestHeaders) {
      client.updateSourceMetadata(requestHeaders, request);
      attributeMetadataList =
          ImmutableList.copyOf(
              client.findAttributes(
                  requestHeaders,
                  AttributeMetadataFilter.newBuilder().addFqn(SPAN_NAME_ATTR).build()));
    } else {
      client.updateSourceMetadata(TEST_TENANT_ID, request);
      attributeMetadataList =
          ImmutableList.copyOf(
              client.findAttributes(
                  TEST_TENANT_ID,
                  AttributeMetadataFilter.newBuilder().addFqn(SPAN_NAME_ATTR).build()));
    }

    assertEquals(1, attributeMetadataList.size());
    Map<String, AttributeSourceMetadata> attributeSourceMetadataMap =
        attributeMetadataList.get(0).getMetadataMap();
    Assertions.assertFalse(attributeSourceMetadataMap.isEmpty());
    assertEquals(
        request.getSourceMetadataMap(),
        attributeSourceMetadataMap.get(AttributeSource.EDS.name()).getSourceMetadataMap());

    if (useRequestHeaders) {
      client.deleteSourceMetadata(
          requestHeaders,
          AttributeSourceMetadataDeleteRequest.newBuilder()
              .setFqn(SPAN_NAME_ATTR)
              .setSource(AttributeSource.EDS)
              .build());
      attributeMetadataList =
          ImmutableList.copyOf(
              client.findAttributes(
                  requestHeaders,
                  AttributeMetadataFilter.newBuilder().addFqn(SPAN_NAME_ATTR).build()));
    } else {
      client.deleteSourceMetadata(
          TEST_TENANT_ID,
          AttributeSourceMetadataDeleteRequest.newBuilder()
              .setFqn(SPAN_NAME_ATTR)
              .setSource(AttributeSource.EDS)
              .build());
      attributeMetadataList =
          ImmutableList.copyOf(
              client.findAttributes(
                  TEST_TENANT_ID,
                  AttributeMetadataFilter.newBuilder().addFqn(SPAN_NAME_ATTR).build()));
    }

    assertEquals(1, attributeMetadataList.size());
    attributeSourceMetadataMap = attributeMetadataList.get(0).getMetadataMap();
    Assertions.assertTrue(attributeSourceMetadataMap.isEmpty());
  }

  @Test
  public void testFindByFilterUseHeaders() {
    testFindByFilter(true);
  }

  @Test
  public void testFindByFilterUseTenantId() {
    testFindByFilter(false);
  }

  private void testFindByFilter(boolean useRequestHeaders) {
    createSampleAttributes(useRequestHeaders, spanNameAttr, spanIdAttr, traceDurationMillis);

    {
      Iterator<AttributeMetadata> attributeMetadataIterator =
          useRequestHeaders
              ? client.findAttributes(requestHeaders, AttributeMetadataFilter.getDefaultInstance())
              : client.findAttributes(TEST_TENANT_ID, AttributeMetadataFilter.getDefaultInstance());
      List<String> attributeMetadataIdList =
          Streams.stream(attributeMetadataIterator)
              .map(AttributeMetadata::getId)
              .sorted()
              .collect(Collectors.toList());
      assertEquals(
          List.of(spanIdAttr.getId(), spanNameAttr.getId(), traceDurationMillis.getId()),
          attributeMetadataIdList);
    }

    {
      Iterator<AttributeMetadata> attributeMetadataIterator =
          useRequestHeaders
              ? client.findAttributes(
                  requestHeaders,
                  AttributeMetadataFilter.newBuilder().addFqn(spanNameAttr.getFqn()).build())
              : client.findAttributes(
                  TEST_TENANT_ID,
                  AttributeMetadataFilter.newBuilder().addFqn(spanNameAttr.getFqn()).build());
      List<String> attributeMetadataIdList =
          Streams.stream(attributeMetadataIterator)
              .map(AttributeMetadata::getId)
              .collect(Collectors.toList());
      assertEquals(List.of(spanNameAttr.getId()), attributeMetadataIdList);
    }

    {
      Iterator<AttributeMetadata> attributeMetadataIterator =
          useRequestHeaders
              ? client.findAttributes(
                  requestHeaders,
                  AttributeMetadataFilter.newBuilder()
                      .addScopeString(spanNameAttr.getScopeString())
                      .addKey(spanNameAttr.getKey())
                      .build())
              : client.findAttributes(
                  TEST_TENANT_ID,
                  AttributeMetadataFilter.newBuilder()
                      .addScopeString(spanNameAttr.getScopeString())
                      .addKey(spanNameAttr.getKey())
                      .build());
      List<String> attributeMetadataIdList =
          Streams.stream(attributeMetadataIterator)
              .map(AttributeMetadata::getId)
              .collect(Collectors.toList());
      assertEquals(List.of(spanNameAttr.getId()), attributeMetadataIdList);
    }
  }

  @Test
  void testUnknownScopeCreateReadDelete() {
    final AttributeMetadata otherNameAttr =
        AttributeMetadata.newBuilder()
            .setId("OTHER.name")
            .setFqn("some fqn")
            .setScopeString("OTHER")
            .setKey("name")
            .setType(AttributeType.ATTRIBUTE)
            .setValueKind(AttributeKind.TYPE_STRING)
            .setDisplayName("Other Name")
            .addSources(AttributeSource.QS)
            .build();

    final AttributeMetadataFilter otherScopeFilter =
        AttributeMetadataFilter.newBuilder().addScopeString("OTHER").build();

    client.create(
        requestHeaders,
        AttributeCreateRequest.newBuilder()
            .addAllAttributes(List.of(spanNameAttr, otherNameAttr))
            .build());

    List<String> attributeMetadataIdList =
        Streams.stream(client.findAttributes(requestHeaders, otherScopeFilter))
            .map(AttributeMetadata::getId)
            .collect(Collectors.toList());

    assertEquals(List.of(otherNameAttr.getId()), attributeMetadataIdList);

    attributeMetadataIdList =
        Streams.stream(
                client.findAttributes(
                    requestHeaders,
                    AttributeMetadataFilter.newBuilder().addScopeString("EVENT").build()))
            .map(AttributeMetadata::getId)
            .collect(Collectors.toList());

    assertEquals(List.of(spanNameAttr.getId()), attributeMetadataIdList);

    attributeMetadataIdList =
        Streams.stream(
                client.findAttributes(
                    requestHeaders,
                    AttributeMetadataFilter.newBuilder().addScope(AttributeScope.EVENT).build()))
            .map(AttributeMetadata::getId)
            .collect(Collectors.toList());

    assertEquals(List.of(spanNameAttr.getId()), attributeMetadataIdList);

    client.delete(requestHeaders, otherScopeFilter);

    assertEquals(
        emptyList(), ImmutableList.copyOf(client.findAttributes(requestHeaders, otherScopeFilter)));
  }

  @Test
  public void testDeleteByFilterUseHeaders() {
    testDeleteByFilter(true);
  }

  @Test
  public void testDeleteByFilterUseTenantId() {
    testDeleteByFilter(false);
  }

  private void testDeleteByFilter(boolean useRequestHeaders) {
    createSampleAttributes(useRequestHeaders, spanNameAttr, spanIdAttr, traceDurationMillis);
    {
      List<AttributeMetadata> attributeMetadataList;
      if (useRequestHeaders) {
        client.delete(requestHeaders, AttributeMetadataFilter.getDefaultInstance());
        attributeMetadataList =
            ImmutableList.copyOf(
                client.findAttributes(
                    requestHeaders, AttributeMetadataFilter.getDefaultInstance()));
      } else {
        client.delete(TEST_TENANT_ID, AttributeMetadataFilter.getDefaultInstance());
        attributeMetadataList =
            ImmutableList.copyOf(
                client.findAttributes(
                    TEST_TENANT_ID, AttributeMetadataFilter.getDefaultInstance()));
      }
      assertEquals(emptyList(), attributeMetadataList);
    }

    createSampleAttributes(useRequestHeaders, spanNameAttr, spanIdAttr, traceDurationMillis);
    {
      Iterator<AttributeMetadata> attributeMetadataIterator;
      if (useRequestHeaders) {
        client.delete(
            requestHeaders,
            AttributeMetadataFilter.newBuilder()
                .addScopeString(spanNameAttr.getScopeString())
                .addKey(spanNameAttr.getKey())
                .build());
        attributeMetadataIterator =
            client.findAttributes(requestHeaders, AttributeMetadataFilter.getDefaultInstance());
      } else {
        client.delete(
            TEST_TENANT_ID,
            AttributeMetadataFilter.newBuilder()
                .addScopeString(spanNameAttr.getScopeString())
                .addKey(spanNameAttr.getKey())
                .build());
        attributeMetadataIterator =
            client.findAttributes(TEST_TENANT_ID, AttributeMetadataFilter.getDefaultInstance());
      }
      List<String> attributeMetadataList =
          Streams.stream(attributeMetadataIterator)
              .map(AttributeMetadata::getId)
              .collect(Collectors.toList());
      assertEquals(2, attributeMetadataList.size());
      Assertions.assertTrue(
          attributeMetadataList.containsAll(
              Arrays.asList(spanIdAttr.getId(), traceDurationMillis.getId())));
    }
  }

  private void createSampleAttributes(
      boolean useRequestHeaders, AttributeMetadata... attributeMetadata) {
    AttributeCreateRequest attributeCreateRequest =
        AttributeCreateRequest.newBuilder()
            .addAllAttributes(Arrays.asList(attributeMetadata))
            .build();
    if (useRequestHeaders) {
      client.create(requestHeaders, attributeCreateRequest);
    } else {
      client.create(TEST_TENANT_ID, attributeCreateRequest);
    }
  }
}
