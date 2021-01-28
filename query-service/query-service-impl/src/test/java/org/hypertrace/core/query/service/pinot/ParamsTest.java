package org.hypertrace.core.query.service.pinot;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Params}
 */
public class ParamsTest {
  @Test
  public void testEquals() {
    Params params = Params.newBuilder().addStringParam("test").addIntegerParam(1)
        .addLongParam(100L).addDoubleParam(1.1d).addFloatParam(0.1f).addByteStringParam(ByteString.copyFrom("bytes".getBytes())).build();
    Assertions.assertEquals(params, params);
    Assertions.assertEquals(params.hashCode(), params.hashCode());
    Assertions.assertNotEquals(params, new Object());

    params = Params.newBuilder().addStringParam("test").build();
    Assertions.assertEquals(params, Params.newBuilder().addStringParam("test").build());
    Assertions.assertNotEquals(params, Params.newBuilder().addStringParam("test").addIntegerParam(1).build());

    params = Params.newBuilder().addStringParam("test1").addFloatParam(0.1f).build();
    Assertions.assertEquals(params, Params.newBuilder().addStringParam("test1").addFloatParam(0.1f).build());
    Assertions.assertNotEquals(params, Params.newBuilder().addFloatParam(0.1f).addDoubleParam(0.3d).build());
  }
}
