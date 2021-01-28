package org.hypertrace.graphql.spaces.dao;

import static org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderDirection.ASC;
import static org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderDirection.DESC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.core.Single;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.request.AttributeRequestBuilder;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.graphql.explorer.dao.ExplorerDao;
import org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey;
import org.hypertrace.graphql.explorer.schema.ExploreResult;
import org.hypertrace.graphql.explorer.schema.ExploreResultSet;
import org.hypertrace.graphql.explorer.schema.Selection;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;
import org.hypertrace.graphql.metric.request.MetricAggregationRequestBuilder;
import org.hypertrace.graphql.spaces.schema.query.SpaceResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExplorerBackedSpacesDaoTest {
  @Mock ExplorerDao mockExplorerDao;
  @Mock AttributeRequestBuilder mockAttributeRequestBuilder;
  @Mock MetricAggregationRequestBuilder mockMetricRequestBuilder;
  @Mock GraphQlRequestContext mockContext;
  @Mock AttributeModel mockAttribute;
  @Mock AttributeRequest mockSpaceAttributeRequest;
  @Mock MetricAggregationRequest mockSpaceCountRequest;
  @Mock Clock mockClock;

  Instant mockEndTime = Instant.parse("2021-02-01T00:00:00.00Z");

  ExplorerBackedSpacesDao dao;

  @BeforeEach
  void beforeEach() {
    this.dao =
        new ExplorerBackedSpacesDao(
            this.mockExplorerDao,
            this.mockAttributeRequestBuilder,
            this.mockMetricRequestBuilder,
            this.mockClock);

    when(this.mockClock.instant()).thenReturn(this.mockEndTime);

    when(this.mockSpaceAttributeRequest.attribute()).thenReturn(this.mockAttribute);

    when(this.mockAttributeRequestBuilder.buildForKey(this.mockContext, "EVENT", "spaceIds"))
        .thenReturn(Single.just(this.mockSpaceAttributeRequest));

    when(this.mockMetricRequestBuilder.build(
            this.mockAttribute, AttributeModelMetricAggregationType.COUNT, Collections.emptyList()))
        .thenReturn(this.mockSpaceCountRequest);
  }

  @Test
  void makesAppropriateRequest() {
    this.dao.getAllSpaces(this.mockContext).blockingSubscribe();

    verify(this.mockExplorerDao)
        .explore(
            argThat(
                request ->
                    request.requestContext().equals(this.mockContext)
                        && request.scope().equals("EVENT")
                        && request.limit() == 100
                        && request.offset() == 0
                        && request
                            .timeRange()
                            .startTime()
                            .equals(Instant.parse("2021-01-01T00:00:00.00Z"))
                        && request.timeRange().endTime().equals(this.mockEndTime)
                        && request.attributeRequests().isEmpty()
                        && request.aggregationRequests().equals(Set.of(this.mockSpaceCountRequest))
                        && request.orderArguments().size() == 1
                        && request.orderArguments().get(0).attribute().equals(this.mockAttribute)
                        && request.orderArguments().get(0).value().key().equals("spaceIds")
                        && request.orderArguments().get(0).value().direction().equals(ASC)
                        && request.filterArguments().isEmpty()
                        && request
                            .groupByAttributeRequests()
                            .equals(Set.of(this.mockSpaceAttributeRequest))
                        && request.timeInterval().isEmpty()
                        && !request.includeRest()
                        && request.spaceId().isEmpty()));
  }

  @Test
  void convertsExplorerResults() {
    ExploreResultSet mockResultSet =
        this.buildMockResultSet(List.of("first-space", "second-space"));
    when(this.mockExplorerDao.explore(any())).thenReturn(Single.just(mockResultSet));

    SpaceResultSet resultSet = this.dao.getAllSpaces(this.mockContext).blockingGet();

    assertEquals(2, resultSet.count());
    assertEquals(2, resultSet.total());
    assertEquals(2, resultSet.results().size());
    assertEquals("first-space", resultSet.results().get(0).name());
    assertEquals("second-space", resultSet.results().get(1).name());
  }

  private ExploreResultSet buildMockResultSet(List<String> spaceNames) {
    ExploreResultSet mockExploreResultSet = mock(ExploreResultSet.class);
    List<ExploreResult> mockResults =
        spaceNames.stream().map(this::buildMockResult).collect(Collectors.toList());
    when(mockExploreResultSet.results()).thenReturn(mockResults);
    return mockExploreResultSet;
  }

  private ExploreResult buildMockResult(String spaceName) {
    ExploreResult mockResult = mock(ExploreResult.class);
    Selection mockSelection = mock(Selection.class);
    when(mockSelection.value()).thenReturn(List.of(spaceName));
    when(mockResult.selectionMap())
        .thenReturn(Map.of(ExploreResultMapKey.basicAttribute("spaceIds"), mockSelection));
    return mockResult;
  }
}
