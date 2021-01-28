package org.hypertrace.graphql.metric.deserialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.common.schema.attributes.MetricAggregationType;
import org.hypertrace.core.graphql.common.schema.results.arguments.order.OrderDirection;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.graphql.metric.schema.argument.AggregatableOrderArgument;

class AggregatableOrderArgumentDeserializationConfig implements ArgumentDeserializationConfig {

  @Override
  public String getArgumentKey() {
    return AggregatableOrderArgument.ARGUMENT_NAME;
  }

  @Override
  public Class<AggregatableOrderArgument> getArgumentSchema() {
    return AggregatableOrderArgument.class;
  }

  @Override
  public List<Module> jacksonModules() {
    return List.of(
        new SimpleModule()
            .addAbstractTypeMapping(
                AggregatableOrderArgument.class, DefaultAggregatableOrderArgument.class));
  }

  @Value
  @Accessors(fluent = true)
  @NoArgsConstructor(force = true)
  private static class DefaultAggregatableOrderArgument implements AggregatableOrderArgument {
    @JsonProperty(ORDER_DIRECTION_NAME)
    OrderDirection direction = OrderDirection.DESC;

    @JsonProperty(ORDER_KEY_NAME)
    String key;

    @JsonProperty(METRIC_AGGREGATION_ORDER_AGGREGATION_TYPE)
    MetricAggregationType aggregation;

    @JsonProperty(METRIC_AGGREGATION_ORDER_AGGREGATION_SIZE)
    Integer size;
  }
}
