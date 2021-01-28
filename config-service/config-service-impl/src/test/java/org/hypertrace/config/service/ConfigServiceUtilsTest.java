package org.hypertrace.config.service;

import static org.hypertrace.config.service.ConfigServiceUtils.DEFAULT_CONTEXT;
import static org.hypertrace.config.service.TestUtils.getConfig1;
import static org.hypertrace.config.service.TestUtils.getConfig2;
import static org.hypertrace.config.service.TestUtils.getExpectedMergedConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.NullValue;
import com.google.protobuf.Value;
import org.junit.jupiter.api.Test;

class ConfigServiceUtilsTest {

  @Test
  void merge() {
    Value config1 = getConfig1();
    Value config2 = getConfig2();

    // test merging 2 config values
    assertEquals(getExpectedMergedConfig(), ConfigServiceUtils.merge(config1, config2));

    // test merging with null value
    Value nullConfigValue = Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build();
    assertEquals(config1, ConfigServiceUtils.merge(config1, nullConfigValue));
    assertEquals(config1, ConfigServiceUtils.merge(nullConfigValue, config1));
  }

  @Test
  void getActualContext() {
    assertEquals(DEFAULT_CONTEXT, ConfigServiceUtils.getActualContext(null));
    assertEquals(DEFAULT_CONTEXT, ConfigServiceUtils.getActualContext(""));
    assertEquals("ctx1", ConfigServiceUtils.getActualContext("ctx1"));
  }
}
