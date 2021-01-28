package org.hypertrace.graphql.explorer.deserialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hypertrace.core.graphql.deserialization.ArgumentDeserializationConfig;
import org.hypertrace.graphql.explorer.schema.argument.GroupByArgument;

public class GroupByArgumentDeserializationConfig implements ArgumentDeserializationConfig {

  @Override
  public String getArgumentKey() {
    return GroupByArgument.ARGUMENT_NAME;
  }

  @Override
  public Class<GroupByArgument> getArgumentSchema() {
    return GroupByArgument.class;
  }

  @Override
  public List<Module> jacksonModules() {
    return List.of(
        new SimpleModule()
            .addAbstractTypeMapping(GroupByArgument.class, DefaultGroupByArgument.class));
  }

  @Value
  @Accessors(fluent = true)
  @NoArgsConstructor(force = true)
  private static class DefaultGroupByArgument implements GroupByArgument {
    @JsonProperty(GROUP_BY_KEYS_KEY)
    List<String> keys;

    @JsonProperty(GROUP_BY_INCLUDE_REST_KEY)
    boolean includeRest;
  }
}
