package org.hypertrace.core.query.service.pinot;

import com.google.protobuf.ByteString;
import org.apache.commons.codec.DecoderException;
import org.hypertrace.core.query.service.api.Value;
import org.hypertrace.core.query.service.api.ValueType;
import org.hypertrace.core.query.service.pinot.converters.DestinationColumnValueConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DestinationColumnValueConverterTest {
  private Value getStringValue(String value) {
    return Value.newBuilder().setString(value).build();
  }

  @Test
  public void testValidNullFieldsForBytesColumn() throws Exception {
    DestinationColumnValueConverter converter = DestinationColumnValueConverter.INSTANCE;

    Value value = converter.convert(getStringValue(""), ValueType.BYTES);
    Assertions.assertNotNull(value);
    Assertions.assertEquals(ValueType.BYTES, value.getValueType());
    Assertions.assertEquals(ByteString.EMPTY, value.getBytes());

    value = converter.convert(getStringValue("null"), ValueType.BYTES);
    Assertions.assertNotNull(value);
    Assertions.assertEquals(ValueType.BYTES, value.getValueType());
    Assertions.assertEquals(ByteString.EMPTY, value.getBytes());

    value = converter.convert(getStringValue("''"), ValueType.BYTES);
    Assertions.assertNotNull(value);
    Assertions.assertEquals(ValueType.BYTES, value.getValueType());
    Assertions.assertEquals(ByteString.EMPTY, value.getBytes());

    value = converter.convert(getStringValue("{}"), ValueType.BYTES);
    Assertions.assertNotNull(value);
    Assertions.assertEquals(ValueType.BYTES, value.getValueType());
    Assertions.assertEquals(ByteString.EMPTY, value.getBytes());
  }

  @Test
  public void testInValidNullFieldsForBytesColumn() {
    DestinationColumnValueConverter converter = DestinationColumnValueConverter.INSTANCE;
    Assertions.assertThrows(DecoderException.class, () -> {
      converter.convert(getStringValue("abc"), ValueType.BYTES);
    });
  }
}
