package org.hypertrace.config.service;

import com.google.common.io.Resources;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class IntegrationTestUtils {

  /**
   * Reads the YAML file and converts it into {@link Value} object
   *
   * @param yamlFileName
   * @return
   * @throws IOException
   */
  public static Value getConfigValue(String yamlFileName) throws IOException {
    String yamlConfigString = getConfigString(yamlFileName);
    Yaml yaml = new Yaml();
    Object yamlObject = yaml.load(yamlConfigString);
    return getConfigValue(yamlObject);
  }

  private static String getConfigString(String resourceName) throws IOException {
    return Resources.toString(Resources.getResource(resourceName), Charset.defaultCharset());
  }

  private static Value getConfigValue(Object yamlObject) {
    Value.Builder configValueBuilder = Value.newBuilder();
    if (yamlObject instanceof Map) {
      Struct.Builder structBuilder = Struct.newBuilder();
      for (Map.Entry<String, Object> entry : ((Map<String, Object>) yamlObject).entrySet()) {
        structBuilder.putFields(entry.getKey(), getConfigValue(entry.getValue()));
      }
      configValueBuilder.setStructValue(structBuilder.build());
    } else if (yamlObject instanceof List) {
      ListValue.Builder listValueBuilder = ListValue.newBuilder();
      for (Object element : (List) yamlObject) {
        listValueBuilder.addValues(getConfigValue(element));
      }
      configValueBuilder.setListValue(listValueBuilder.build());
    } else {
      configValueBuilder.setStringValue(String.valueOf(yamlObject));
    }
    return configValueBuilder.build();
  }
}
