package org.hypertrace.core.attribute.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.ServiceException;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.v1.AggregateFunction;
import org.hypertrace.core.attribute.service.v1.AttributeDefinition;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeMetadataFilter;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.attribute.service.v1.AttributeType;
import org.hypertrace.core.attribute.service.v1.Empty;
import org.hypertrace.core.documentstore.Collection;
import org.hypertrace.core.documentstore.Document;
import org.hypertrace.core.documentstore.Filter;
import org.hypertrace.core.documentstore.Query;
import org.hypertrace.core.grpcutils.context.RequestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class AttributeServiceImplTest {
  @Test
  public void testFindAll() {
    RequestContext requestContext = mock(RequestContext.class);
    when(requestContext.getTenantId()).thenReturn(Optional.of("test-tenant-id"));
    Context ctx = Context.current().withValue(RequestContext.CURRENT, requestContext);

    Context previous = ctx.attach();
    try {
      Collection collection = mock(Collection.class);
      StreamObserver<AttributeMetadata> responseObserver = mock(StreamObserver.class);

      Document document1 =
          createMockDocument(
              "__root",
              "name",
              AttributeScope.EVENT,
              AttributeType.ATTRIBUTE,
              AttributeKind.TYPE_STRING);
      Document document2 =
          createMockDocument(
              "__root",
              "duration",
              AttributeScope.EVENT,
              AttributeType.METRIC,
              AttributeKind.TYPE_INT64);
      List<Document> documents = List.of(document1, document2);
      when(collection.search(any(Query.class))).thenReturn(documents.iterator());

      AttributeServiceImpl attributeService = new AttributeServiceImpl(collection);

      attributeService.findAll(Empty.newBuilder().build(), responseObserver);

      ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
      verify(collection, times(1)).search(queryCaptor.capture());

      Filter filter = queryCaptor.getValue().getFilter();
      Assertions.assertEquals("tenant_id", filter.getFieldName());
      Assertions.assertEquals(List.of("__root", "test-tenant-id"), filter.getValue());

      ArgumentCaptor<AttributeMetadata> attributeMetadataArgumentCaptor =
          ArgumentCaptor.forClass(AttributeMetadata.class);
      verify(responseObserver, times(2)).onNext(attributeMetadataArgumentCaptor.capture());

      List<AttributeMetadata> attributeMetadataList =
          attributeMetadataArgumentCaptor.getAllValues();
      Assertions.assertEquals(2, attributeMetadataList.size());
      AttributeMetadata attributeMetadata1 =
          AttributeMetadata.newBuilder()
              .setFqn("EVENT.name")
              .setId("EVENT.name")
              .setKey("name")
              .setScope(AttributeScope.EVENT)
              .setScopeString(AttributeScope.EVENT.name())
              .setDisplayName("EVENT name")
              .setValueKind(AttributeKind.TYPE_STRING)
              .setDefinition(AttributeDefinition.getDefaultInstance())
              .setGroupable(true)
              .setType(AttributeType.ATTRIBUTE)
              // Add default aggregations. See SupportedAggregationsDecorator
              .addAllSupportedAggregations(List.of(AggregateFunction.DISTINCT_COUNT))
              .build();
      AttributeMetadata attributeMetadata2 =
          AttributeMetadata.newBuilder()
              .setFqn("EVENT.duration")
              .setId("EVENT.duration")
              .setKey("duration")
              .setScope(AttributeScope.EVENT)
              .setScopeString(AttributeScope.EVENT.name())
              .setDisplayName("EVENT duration")
              .setGroupable(false)
              .setDefinition(AttributeDefinition.getDefaultInstance())
              .setValueKind(AttributeKind.TYPE_INT64)
              .setType(AttributeType.METRIC)
              // Add default aggregations. See SupportedAggregationsDecorator
              .addAllSupportedAggregations(
                  List.of(
                      AggregateFunction.SUM,
                      AggregateFunction.MIN,
                      AggregateFunction.MAX,
                      AggregateFunction.AVG,
                      AggregateFunction.AVGRATE,
                      AggregateFunction.PERCENTILE))
              .build();
      Assertions.assertEquals(attributeMetadata1, attributeMetadataList.get(0));
      Assertions.assertEquals(attributeMetadata2, attributeMetadataList.get(1));

      verify(responseObserver, times(1)).onCompleted();
      verify(responseObserver, never()).onError(any(Throwable.class));
    } finally {
      ctx.detach(previous);
    }
  }

  @Test
  public void testFindAllNoTenantId() {
    RequestContext requestContext = mock(RequestContext.class);
    when(requestContext.getTenantId()).thenReturn(Optional.empty());
    Context ctx = Context.current().withValue(RequestContext.CURRENT, requestContext);

    Context previous = ctx.attach();
    try {
      Collection collection = mock(Collection.class);
      StreamObserver<AttributeMetadata> responseObserver = mock(StreamObserver.class);

      AttributeServiceImpl attributeService = new AttributeServiceImpl(collection);

      attributeService.findAll(Empty.newBuilder().build(), responseObserver);

      verify(collection, never()).search(any(Query.class));
      verify(responseObserver, never()).onNext(any(AttributeMetadata.class));
      verify(responseObserver, never()).onCompleted();
      verify(responseObserver, times(1)).onError(any(ServiceException.class));
    } finally {
      ctx.detach(previous);
    }
  }

  @Test
  public void testFindAttributes() {
    RequestContext requestContext = mock(RequestContext.class);
    when(requestContext.getTenantId()).thenReturn(Optional.of("test-tenant-id"));
    Context ctx = Context.current().withValue(RequestContext.CURRENT, requestContext);

    Context previous = ctx.attach();
    try {
      Collection collection = mock(Collection.class);
      StreamObserver<AttributeMetadata> responseObserver = mock(StreamObserver.class);

      Document document1 =
          createMockDocument(
              "__root",
              "name",
              AttributeScope.EVENT,
              AttributeType.ATTRIBUTE,
              AttributeKind.TYPE_STRING);
      Document document2 =
          createMockDocument(
              "__root",
              "duration",
              AttributeScope.EVENT,
              AttributeType.METRIC,
              AttributeKind.TYPE_INT64);
      List<Document> documents = List.of(document1, document2);
      when(collection.search(any(Query.class))).thenReturn(documents.iterator());

      AttributeServiceImpl attributeService = new AttributeServiceImpl(collection);

      List<String> fqnList = List.of("EVENT.name", "EVENT.id");
      List<String> keyList = List.of("name", "startTime", "duration");

      AttributeMetadataFilter attributeMetadataFilter =
          AttributeMetadataFilter.newBuilder()
              .addAllFqn(fqnList)
              .addAllKey(keyList)
              .addAllScope(
                  List.of(AttributeScope.TRACE, AttributeScope.EVENT, AttributeScope.BACKEND))
              .addScopeString("OTHER")
              .build();

      List<String> allScopes =
          List.of(
              "OTHER",
              AttributeScope.TRACE.name(),
              AttributeScope.EVENT.name(),
              AttributeScope.BACKEND.name());

      attributeService.findAttributes(attributeMetadataFilter, responseObserver);

      ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
      verify(collection, times(1)).search(queryCaptor.capture());

      Filter filter = queryCaptor.getValue().getFilter();
      // The structure of the filters is an and(^) filter chain that looks like this:
      // (((tenant_id ^ fqn) ^ (scope_string | scope)) ^ key)
      Assertions.assertEquals(Filter.Op.AND, filter.getOp());
      Assertions.assertEquals(Filter.Op.IN, filter.getChildFilters()[1].getOp());
      Assertions.assertEquals("key", filter.getChildFilters()[1].getFieldName());
      Assertions.assertEquals(keyList, filter.getChildFilters()[1].getValue());

      Assertions.assertEquals(Filter.Op.AND, filter.getChildFilters()[0].getOp());
      Filter scopeFilter = filter.getChildFilters()[0].getChildFilters()[1];
      Assertions.assertEquals(Filter.Op.OR, scopeFilter.getOp());

      Assertions.assertEquals(Filter.Op.IN, scopeFilter.getChildFilters()[0].getOp());
      Assertions.assertEquals("scope_string", scopeFilter.getChildFilters()[0].getFieldName());
      Assertions.assertEquals(allScopes, scopeFilter.getChildFilters()[0].getValue());

      Assertions.assertEquals(Filter.Op.IN, scopeFilter.getChildFilters()[1].getOp());
      Assertions.assertEquals("scope", scopeFilter.getChildFilters()[1].getFieldName());
      Assertions.assertEquals(allScopes, scopeFilter.getChildFilters()[1].getValue());

      Assertions.assertEquals(
          Filter.Op.AND, filter.getChildFilters()[0].getChildFilters()[0].getOp());
      Assertions.assertEquals(
          Filter.Op.IN,
          filter.getChildFilters()[0].getChildFilters()[0].getChildFilters()[1].getOp());
      Assertions.assertEquals(
          "fqn",
          filter.getChildFilters()[0].getChildFilters()[0].getChildFilters()[1].getFieldName());
      Assertions.assertEquals(
          fqnList,
          filter.getChildFilters()[0].getChildFilters()[0].getChildFilters()[1].getValue());

      Assertions.assertEquals(
          Filter.Op.IN,
          filter.getChildFilters()[0].getChildFilters()[0].getChildFilters()[0].getOp());
      Assertions.assertEquals(
          List.of("__root", "test-tenant-id"),
          filter.getChildFilters()[0].getChildFilters()[0].getChildFilters()[0].getValue());

      ArgumentCaptor<AttributeMetadata> attributeMetadataArgumentCaptor =
          ArgumentCaptor.forClass(AttributeMetadata.class);
      verify(responseObserver, times(2)).onNext(attributeMetadataArgumentCaptor.capture());

      List<AttributeMetadata> attributeMetadataList =
          attributeMetadataArgumentCaptor.getAllValues();
      Assertions.assertEquals(2, attributeMetadataList.size());
      AttributeMetadata attributeMetadata1 =
          AttributeMetadata.newBuilder()
              .setFqn("EVENT.name")
              .setId("EVENT.name")
              .setKey("name")
              .setScope(AttributeScope.EVENT)
              .setScopeString(AttributeScope.EVENT.name())
              .setDisplayName("EVENT name")
              .setValueKind(AttributeKind.TYPE_STRING)
              .setDefinition(AttributeDefinition.getDefaultInstance())
              .setGroupable(true)
              .setType(AttributeType.ATTRIBUTE)
              // Add default aggregations. See SupportedAggregationsDecorator
              .addAllSupportedAggregations(List.of(AggregateFunction.DISTINCT_COUNT))
              .build();
      AttributeMetadata attributeMetadata2 =
          AttributeMetadata.newBuilder()
              .setFqn("EVENT.duration")
              .setId("EVENT.duration")
              .setKey("duration")
              .setScope(AttributeScope.EVENT)
              .setScopeString(AttributeScope.EVENT.name())
              .setDisplayName("EVENT duration")
              .setGroupable(false)
              .setValueKind(AttributeKind.TYPE_INT64)
              .setDefinition(AttributeDefinition.getDefaultInstance())
              .setType(AttributeType.METRIC)
              // Add default aggregations. See SupportedAggregationsDecorator
              .addAllSupportedAggregations(
                  List.of(
                      AggregateFunction.SUM,
                      AggregateFunction.MIN,
                      AggregateFunction.MAX,
                      AggregateFunction.AVG,
                      AggregateFunction.AVGRATE,
                      AggregateFunction.PERCENTILE))
              .build();
      Assertions.assertEquals(attributeMetadata1, attributeMetadataList.get(0));
      Assertions.assertEquals(attributeMetadata2, attributeMetadataList.get(1));

      verify(responseObserver, times(1)).onCompleted();
      verify(responseObserver, never()).onError(any(Throwable.class));
    } finally {
      ctx.detach(previous);
    }
  }

  @Test
  public void testFindAttributesNoTenantId() {
    RequestContext requestContext = mock(RequestContext.class);
    when(requestContext.getTenantId()).thenReturn(Optional.empty());
    Context ctx = Context.current().withValue(RequestContext.CURRENT, requestContext);

    Context previous = ctx.attach();
    try {
      Collection collection = mock(Collection.class);
      StreamObserver<AttributeMetadata> responseObserver = mock(StreamObserver.class);

      AttributeServiceImpl attributeService = new AttributeServiceImpl(collection);

      attributeService.findAttributes(
          AttributeMetadataFilter.newBuilder().build(), responseObserver);

      verify(collection, never()).search(any(Query.class));
      verify(responseObserver, never()).onNext(any(AttributeMetadata.class));
      verify(responseObserver, never()).onCompleted();
      verify(responseObserver, times(1)).onError(any(ServiceException.class));
    } finally {
      ctx.detach(previous);
    }
  }

  private Document createMockDocument(
      String tenantId, String key, AttributeScope scope, AttributeType type, AttributeKind kind) {
    Document document = mock(Document.class);

    StringBuilder sb = new StringBuilder();
    sb.append("{\"fqn\":\"")
        .append(scope.name())
        .append(".")
        .append(key)
        .append("\",\"scope\":\"")
        .append(scope.name())
        .append("\",\"key\":\"")
        .append(key)
        .append("\",\"type\":\"")
        .append(type.name())
        .append("\",\"value_kind\":\"")
        .append(kind.name())
        .append("\",\"tenant_id\":\"")
        .append(tenantId)
        .append("\",\"display_name\":\"")
        .append(scope.name())
        .append(" ")
        .append(key)
        .append("\"}");

    when(document.toJson()).thenReturn(sb.toString());
    return document;
  }
}
