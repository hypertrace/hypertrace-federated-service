package org.hypertrace.graphql.explorer.dao;

import static io.reactivex.rxjava3.core.Observable.merge;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.attributes.AttributeModel;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.attributes.AttributeModelType;
import org.hypertrace.core.graphql.common.request.AttributeRequest;
import org.hypertrace.core.graphql.common.schema.attributes.AttributeType;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.utils.CollectorUtils;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Row;
import org.hypertrace.gateway.service.v1.common.Value;
import org.hypertrace.gateway.service.v1.explore.ColumnName;
import org.hypertrace.graphql.explorer.fetcher.ExploreResultMapKey;
import org.hypertrace.graphql.explorer.request.ExploreRequest;
import org.hypertrace.graphql.explorer.schema.Selection;
import org.hypertrace.graphql.metric.request.MetricAggregationRequest;

class GatewayServiceSelectionMapConverter {
  // TODO we should not be relying on a proto enum's name, but required in current API
  private static final String WELL_KNOWN_INTERVAL_KEY = ColumnName.INTERVAL_START_TIME.name();

  private final Converter<Value, Object> valueConverter;
  private final Converter<AttributeModelType, AttributeType> attributeTypeConverter;
  private final Converter<AttributeModelMetricAggregationType, MetricAggregationType>
      aggregationTypeConverter;

  @Inject
  GatewayServiceSelectionMapConverter(
      Converter<Value, Object> valueConverter,
      Converter<AttributeModelType, AttributeType> attributeTypeConverter,
      Converter<AttributeModelMetricAggregationType, MetricAggregationType>
          aggregationTypeConverter) {
    this.valueConverter = valueConverter;
    this.attributeTypeConverter = attributeTypeConverter;
    this.aggregationTypeConverter = aggregationTypeConverter;
  }

  Single<Map<ExploreResultMapKey, Selection>> convert(ExploreRequest request, Row row) {
    return merge(
            this.buildAttributeMapEntries(request.attributeRequests(), row),
            this.buildAttributeMapEntries(request.groupByAttributeRequests(), row),
            this.buildAggregationMapEntries(request.aggregationRequests(), row),
            this.buildIntervalStartMapEntry(row).toObservable())
        .distinct()
        .collect(CollectorUtils.immutableMapEntryCollector());
  }

  private Observable<Entry<ExploreResultMapKey, Selection>> buildAttributeMapEntries(
      Set<AttributeRequest> attributeRequests, Row row) {
    return Observable.fromIterable(attributeRequests)
        .flatMapSingle(request -> this.buildAttributeMapEntry(request, row));
  }

  private Single<Entry<ExploreResultMapKey, Selection>> buildAttributeMapEntry(
      AttributeRequest attributeRequest, Row row) {
    return this.valueConverter
        .convert(row.getColumnsOrThrow(attributeRequest.alias()))
        .flatMap(value -> this.buildSelection(attributeRequest.attribute(), value))
        .map(
            selection ->
                Map.entry(
                    ExploreResultMapKey.basicAttribute(attributeRequest.attribute().key()),
                    selection));
  }

  private Observable<Entry<ExploreResultMapKey, Selection>> buildAggregationMapEntries(
      Set<MetricAggregationRequest> aggregationRequests, Row row) {

    return Observable.fromIterable(aggregationRequests)
        .flatMapSingle(request -> this.buildAggregationMapEntry(request, row));
  }

  private Single<Entry<ExploreResultMapKey, Selection>> buildAggregationMapEntry(
      MetricAggregationRequest aggregationRequest, Row row) {
    return this.valueConverter
        .convert(row.getColumnsOrThrow(aggregationRequest.alias()))
        .flatMap(value -> this.buildSelection(aggregationRequest.attribute(), value))
        .zipWith(
            this.aggregationTypeConverter.convert(aggregationRequest.aggregation()),
            (selection, aggregation) ->
                Map.entry(
                    ExploreResultMapKey.forAggregationRequest(aggregationRequest, aggregation),
                    selection));
  }

  private Maybe<Entry<ExploreResultMapKey, Selection>> buildIntervalStartMapEntry(Row row) {
    return Maybe.fromOptional(
            Optional.ofNullable(row.getColumnsOrDefault(WELL_KNOWN_INTERVAL_KEY, null)))
        .flatMapSingle(this.valueConverter::convert)
        .cast(Long.class)
        .map(Instant::ofEpochMilli)
        .map(instant -> new ConvertedSelection(AttributeType.TIMESTAMP, instant))
        .map(selection -> Map.entry(ExploreResultMapKey.intervalStart(), selection));
  }

  private Single<Selection> buildSelection(AttributeModel attributeModel, Object value) {
    return this.attributeTypeConverter
        .convert(attributeModel.type())
        .map(type -> new ConvertedSelection(type, value));
  }

  @lombok.Value
  @Accessors(fluent = true)
  private static class ConvertedSelection implements Selection {
    AttributeType type;
    Object value;
  }
}
