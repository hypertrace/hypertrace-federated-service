package org.hypertrace.graphql.explorer.dao;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.gateway.service.v1.common.Row;
import org.hypertrace.gateway.service.v1.explore.ExploreResponse;
import org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey;
import org.hypertrace.graphql.explorer.request.ExploreRequest;
import org.hypertrace.graphql.explorer.schema.ExploreResult;
import org.hypertrace.graphql.explorer.schema.ExploreResultSet;
import org.hypertrace.graphql.explorer.schema.Selection;

class GatewayServiceExploreResponseConverter {

  private final GatewayServiceSelectionMapConverter selectionMapConverter;

  @Inject
  GatewayServiceExploreResponseConverter(
      GatewayServiceSelectionMapConverter selectionMapConverter) {
    this.selectionMapConverter = selectionMapConverter;
  }

  Single<ExploreResultSet> convert(ExploreRequest request, ExploreResponse response) {
    return Observable.fromIterable(response.getRowList())
        .flatMapSingle(row -> this.convertRow(request, row))
        .toList()
        .map(results -> new ConvertedExploreResultSet(results, results.size()));
  }

  private Single<ExploreResult> convertRow(ExploreRequest request, Row row) {
    return this.selectionMapConverter
        .convert(request, row)
        .map(this::buildResult);
  }

  private ExploreResult buildResult(Map<ExploreResultMapKey, Selection> selectionMap) {

    Optional<Instant> intervalStart =
        Optional.ofNullable(selectionMap.get(ExploreResultMapKey.intervalStart()))
            .map(Selection::value)
            .map(Instant.class::cast);

    return new ConvertedExploreResult(
        selectionMap, intervalStart.orElse(null));
  }

  @Value
  @Accessors(fluent = true)
  private static class ConvertedExploreResult implements ExploreResult {
    Map<ExploreResultMapKey, Selection> selectionMap;
    Instant intervalStart;
  }

  @Value
  @Accessors(fluent = true)
  private static class ConvertedExploreResultSet implements ExploreResultSet {
    List<ExploreResult> results;
    long count;

    @Override
    public long total() {
      //  Total not currently exposed by gateway
      return this.results.size();
    }
  }
}
