package org.hypertrace.graphql.utils.metrics.gateway;

import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.hypertrace.core.graphql.common.utils.Converter;
import org.hypertrace.gateway.service.v1.common.Period;

class PeriodConverter implements Converter<Duration, Period> {
  @Override
  public Single<Period> convert(Duration period) {
    return Single.just(
        Period.newBuilder()
            .setValue(Math.toIntExact(period.toSeconds()))
            .setUnit(ChronoUnit.SECONDS.name())
            .build());
  }
}
