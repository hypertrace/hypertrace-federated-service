package org.hypertrace.core.query.service.pinot.converters;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.hypertrace.core.query.service.api.Value;
import org.hypertrace.core.query.service.api.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter to convert the given value to a new value of type {@link Value} of {@link ValueType}
 */
public class DestinationColumnValueConverter {
  private static final Logger LOG = LoggerFactory.getLogger(DestinationColumnValueConverter.class);

  public static final DestinationColumnValueConverter INSTANCE = new DestinationColumnValueConverter();
  private static final String EMPTY = "";

  private DestinationColumnValueConverter() {
  }

  public Value convert(Value value, ValueType valueType) throws Exception {
    // Currently, only Pinot's BYTES columns needs transformation since they're actually
    // used as strings throughout.
    // TODO: Fix the upper layers to use this BYTES columns as bytes instead of string.
    if (valueType != ValueType.BYTES) {
      return value;
    }

    switch (value.getValueType()) {
      case STRING:
        Value.Builder valueBuilder = Value.newBuilder();
        String inValue = value.getString();
        valueBuilder.setBytes(convertToByteString(inValue));
        valueBuilder.setValueType(ValueType.BYTES);
        return valueBuilder.build();

      case STRING_ARRAY:
        valueBuilder = Value.newBuilder();
        // Convert the string array into byte array type.
        for (String s : value.getStringArrayList()) {
          valueBuilder.addBytesArray(convertToByteString(s));
        }
        valueBuilder.setValueType(ValueType.BYTES_ARRAY);
        return valueBuilder.build();

      default:
        String msg = String.format("Unsupported value of type: %s while transforming to BYTES.",
            value.getValueType().name());
        LOG.warn(msg);
        throw new IllegalArgumentException(msg);
    }
  }

  private ByteString convertToByteString(String inValue) throws DecoderException {
    String outValue = (Strings.isNullOrEmpty(inValue) || inValue.trim().equals("null") ||
        inValue.trim().equals("''") || inValue.trim().equals("{}")) ? EMPTY : inValue;
    byte[] bytes = Hex.decodeHex(outValue);
    return ByteString.copyFrom(bytes);
  }
}
