package org.hypertrace.graphql.utils.metrics.gateway;

import io.reactivex.rxjava3.core.Single;
import java.util.UnknownFormatConversionException;
import org.hypertrace.core.graphql.attributes.AttributeModelMetricAggregationType;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.FunctionType;

class FunctionTypeConverter
    implements Converter<AttributeModelMetricAggregationType, FunctionType> {
  @Override
  public Single<FunctionType> convert(AttributeModelMetricAggregationType aggregationType) {
    switch (aggregationType) {
      case COUNT:
        return Single.just(FunctionType.COUNT);
      case AVG:
        return Single.just(FunctionType.AVG);
      case SUM:
        return Single.just(FunctionType.SUM);
      case MIN:
        return Single.just(FunctionType.MIN);
      case MAX:
        return Single.just(FunctionType.MAX);
      case AVGRATE:
        return Single.just(FunctionType.AVGRATE);
      case PERCENTILE:
        return Single.just(FunctionType.PERCENTILE);
      case DISTINCT_COUNT:
        return Single.just(FunctionType.DISTINCTCOUNT);
      default:
        return Single.error(
            new UnknownFormatConversionException(
                String.format(
                    "Unable to convert unknown aggregation type '%s'", aggregationType.name())));
    }
  }
}
