package org.hypertrace.core.query.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.core.Single;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.hypertrace.core.query.service.QueryTransformation.QueryTransformationContext;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryTransformationPipelineTest {

  private static final String TEST_TENANT_ID = "test-tenant-id";
  QueryRequest originalRequest = QueryRequest.newBuilder().build();

  @Test
  void returnsOriginalRequestIfNoTransformationsProvided() {
    assertSame(
        originalRequest,
        new QueryTransformationPipeline(Collections.emptySet())
            .transform(this.originalRequest, TEST_TENANT_ID)
            .blockingGet());
  }

  @Test
  void callsEachTransformationWithPreviousResult() {
    QueryTransformation firstTransformation = mock(QueryTransformation.class);
    QueryRequest firstResult = QueryRequest.newBuilder().setLimit(1).build();
    when(firstTransformation.transform(
            same(originalRequest), any(QueryTransformationContext.class)))
        .thenReturn(Single.just(firstResult));

    QueryTransformation secondTransformation = mock(QueryTransformation.class);
    QueryRequest secondResult = QueryRequest.newBuilder().setLimit(2).build();
    when(secondTransformation.transform(same(firstResult), any(QueryTransformationContext.class)))
        .thenReturn(Single.just(secondResult));

    assertSame(
        secondResult,
        new QueryTransformationPipeline(
                new LinkedHashSet<>(List.of(firstTransformation, secondTransformation)))
            .transform(this.originalRequest, TEST_TENANT_ID)
            .blockingGet());
  }
}
