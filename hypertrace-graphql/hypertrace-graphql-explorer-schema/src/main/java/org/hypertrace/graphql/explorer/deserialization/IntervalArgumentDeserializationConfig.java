package org.hypertrace.graphql.explorer.deserialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.graphql.explorer.schema.argument.IntervalArgument;

public class IntervalArgumentDeserializationConfig implements ArgumentDeserializationConfig {

  @Override
  public String getArgumentKey() {
    return IntervalArgument.ARGUMENT_NAME;
  }

  @Override
  public Class<IntervalArgument> getArgumentSchema() {
    return IntervalArgument.class;
  }

  @Override
  public List<Module> jacksonModules() {
    return List.of(
        new SimpleModule()
            .addAbstractTypeMapping(IntervalArgument.class, DefaultIntervalArgument.class));
  }

  @Value
  @Accessors(fluent = true)
  @NoArgsConstructor(force = true)
  private static class DefaultIntervalArgument implements IntervalArgument {
    @JsonProperty(INTERVAL_SIZE_KEY)
    int size;

    @JsonProperty(INTERVAL_UNITS_KEY)
    TimeUnit units;
  }
}
