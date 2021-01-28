package org.hypertrace.config.proto.converter;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;

public class ConfigProtoConverter {
  private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer();
  private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser().ignoringUnknownFields();

  public static void mergeFromValue(Value value, Message.Builder builder)
      throws InvalidProtocolBufferException {
    JSON_PARSER.merge(JSON_PRINTER.print(value), builder);
  }

  public static Value convertToValue(MessageOrBuilder messageOrBuilder)
      throws InvalidProtocolBufferException {
    Value.Builder valueBuilder = Value.newBuilder();
    JSON_PARSER.merge(JSON_PRINTER.print(messageOrBuilder), valueBuilder);
    return valueBuilder.build();
  }
}
