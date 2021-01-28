package org.hypertrace.config.service;

import static org.hypertrace.config.service.ConfigServiceUtils.DEFAULT_CONTEXT;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

public class TestUtils {

  public static final String RESOURCE_NAME = "foo";
  public static final String RESOURCE_NAMESPACE = "bar";
  public static final String TENANT_ID = "tenant1";
  public static final String CONTEXT1 = "ctx1";

  public static ConfigResource getConfigResource() {
    return new ConfigResource(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_ID, DEFAULT_CONTEXT);
  }

  public static ConfigResource getConfigResource(String context) {
    return new ConfigResource(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_ID, context);
  }

  /**
   * This method returns the {@link Value} corresponding to the below JSON { "k1": 10, "k2":"v2",
   * "k3": [ { "k31": "v31" }, { "k32": "v32" } ] }
   */
  public static Value getConfig1() {
    ListValue listValueForK3 = ListValue.newBuilder()
        .addValues(Value.newBuilder().setStructValue(getStructForKeyValue("k31", "v31")).build())
        .addValues(Value.newBuilder().setStructValue(getStructForKeyValue("k32", "v32")).build())
        .build();
    Struct struct = Struct.newBuilder()
        .putFields("k1", Value.newBuilder().setNumberValue(10).build())
        .putFields("k2", Value.newBuilder().setStringValue("v2").build())
        .putFields("k3", Value.newBuilder().setListValue(listValueForK3).build())
        .build();

    return Value.newBuilder().setStructValue(struct).build();
  }

  /**
   * This method returns the {@link Value} corresponding to the below JSON { "k1": 20, "k3":[ {
   * "k33": "v33" } ], "k4": "v4" }
   */
  public static Value getConfig2() {
    ListValue listValueForK3 = ListValue.newBuilder()
        .addValues(Value.newBuilder().setStructValue(getStructForKeyValue("k33", "v33")).build())
        .build();
    Struct struct = Struct.newBuilder()
        .putFields("k1", Value.newBuilder().setNumberValue(20).build())
        .putFields("k3", Value.newBuilder().setListValue(listValueForK3).build())
        .putFields("k4", Value.newBuilder().setStringValue("v4").build())
        .build();

    return Value.newBuilder().setStructValue(struct).build();
  }

  /**
   * This method returns the {@link Value} corresponding to the below JSON (obtained by merging
   * config1 and config2) { "k1": 20, "k2": "v2", "k3": [ { "k33": "v33" } ], "k4": "v4" }
   */
  public static Value getExpectedMergedConfig() {
    ListValue listValueForK3 = ListValue.newBuilder()
        .addValues(Value.newBuilder().setStructValue(getStructForKeyValue("k33", "v33")).build())
        .build();
    Struct struct = Struct.newBuilder()
        .putFields("k1", Value.newBuilder().setNumberValue(20).build())
        .putFields("k2", Value.newBuilder().setStringValue("v2").build())
        .putFields("k3", Value.newBuilder().setListValue(listValueForK3).build())
        .putFields("k4", Value.newBuilder().setStringValue("v4").build())
        .build();

    return Value.newBuilder().setStructValue(struct).build();
  }

  private static Struct getStructForKeyValue(String key, String value) {
    return Struct.newBuilder()
        .putFields(key, Value.newBuilder().setStringValue(value).build())
        .build();
  }
}
