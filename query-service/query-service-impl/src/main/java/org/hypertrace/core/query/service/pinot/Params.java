package org.hypertrace.core.query.service.pinot;

import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the params that need to be set in the PreparedStatement for constructing the final PQL
 * query
 */
public class Params {

  // Map of index to the corresponding param value
  private final Map<Integer, Integer> integerParams;
  private final Map<Integer, Long> longParams;
  private final Map<Integer, String> stringParams;
  private final Map<Integer, Float> floatParams;
  private final Map<Integer, Double> doubleParams;
  private final Map<Integer, ByteString> byteStringParams;

  private Params(
      Map<Integer, Integer> integerParams,
      Map<Integer, Long> longParams,
      Map<Integer, String> stringParams,
      Map<Integer, Float> floatParams,
      Map<Integer, Double> doubleParams,
      Map<Integer, ByteString> byteStringParams) {
    this.integerParams = integerParams;
    this.longParams = longParams;
    this.stringParams = stringParams;
    this.floatParams = floatParams;
    this.doubleParams = doubleParams;
    this.byteStringParams = byteStringParams;
  }

  public Map<Integer, Integer> getIntegerParams() {
    return integerParams;
  }

  public Map<Integer, Long> getLongParams() {
    return longParams;
  }

  public Map<Integer, String> getStringParams() {
    return stringParams;
  }

  public Map<Integer, Float> getFloatParams() {
    return floatParams;
  }

  public Map<Integer, Double> getDoubleParams() {
    return doubleParams;
  }

  public Map<Integer, ByteString> getByteStringParams() {
    return byteStringParams;
  }

  @Override
  public String toString() {
    return "Params{" +
        "integerParams=" + integerParams +
        ", longParams=" + longParams +
        ", stringParams=" + stringParams +
        ", floatParams=" + floatParams +
        ", doubleParams=" + doubleParams +
        ", byteStringParams=" + byteStringParams +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Params params = (Params) o;

    if (!integerParams.equals(params.integerParams)) {
      return false;
    }
    if (!longParams.equals(params.longParams)) {
      return false;
    }
    if (!stringParams.equals(params.stringParams)) {
      return false;
    }
    if (!floatParams.equals(params.floatParams)) {
      return false;
    }
    if (!doubleParams.equals(params.doubleParams)) {
      return false;
    }
    return byteStringParams.equals(params.byteStringParams);
  }

  @Override
  public int hashCode() {
    int result = integerParams.hashCode();
    result = 31 * result + longParams.hashCode();
    result = 31 * result + stringParams.hashCode();
    result = 31 * result + floatParams.hashCode();
    result = 31 * result + doubleParams.hashCode();
    result = 31 * result + byteStringParams.hashCode();
    return result;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private int nextIndex;
    private final Map<Integer, Integer> integerParams;
    private final Map<Integer, Long> longParams;
    private final Map<Integer, String> stringParams;
    private final Map<Integer, Float> floatParams;
    private final Map<Integer, Double> doubleParams;
    private final Map<Integer, ByteString> byteStringParams;

    private Builder() {
      nextIndex = 0;
      integerParams = new HashMap<>();
      longParams = new HashMap<>();
      stringParams = new HashMap<>();
      floatParams = new HashMap<>();
      doubleParams = new HashMap<>();
      byteStringParams = new HashMap<>();
    }

    public Builder addIntegerParam(int paramValue) {
      integerParams.put(nextIndex++, paramValue);
      return this;
    }

    public Builder addLongParam(long paramValue) {
      longParams.put(nextIndex++, paramValue);
      return this;
    }

    public Builder addStringParam(String paramValue) {
      stringParams.put(nextIndex++, paramValue);
      return this;
    }

    public Builder addFloatParam(float paramValue) {
      floatParams.put(nextIndex++, paramValue);
      return this;
    }

    public Builder addDoubleParam(double paramValue) {
      doubleParams.put(nextIndex++, paramValue);
      return this;
    }

    public Builder addByteStringParam(ByteString paramValue) {
      byteStringParams.put(nextIndex++, paramValue);
      return this;
    }

    public Params build() {
      return new Params(integerParams, longParams, stringParams, floatParams, doubleParams,
          byteStringParams);
    }
  }
}
