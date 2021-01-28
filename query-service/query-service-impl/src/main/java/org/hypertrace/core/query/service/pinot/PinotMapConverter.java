package org.hypertrace.core.query.service.pinot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinotMapConverter {
  // This is how empty list is represented in Pinot
  private static final String PINOT_EMPTY_LIST = "[\"\"]";
  private static final Logger LOG = LoggerFactory.getLogger(PinotMapConverter.class);
  private static final TypeReference<List<String>> listOfString = new TypeReference<>() {};

  private final ObjectMapper objectMapper;

  public PinotMapConverter() {
    this.objectMapper = new ObjectMapper();
  }

  String merge(String keyData, String valueData) throws IOException {
    Map<String, String> map = new HashMap<>();
    // default should not be null
    if (keyData == null || valueData == null) {
      // throw IOException so that it can be caught be the caller and provide additional
      // context
      throw new IOException("Key Data or Value Data of this map is null.");
    }

    List<String> keys;
    if (PINOT_EMPTY_LIST.equals(keyData)) {
      keys = new ArrayList<>();
    } else {
      try {
        keys = objectMapper.readValue(keyData, listOfString);
      } catch (IOException e) {
        LOG.error(
            "Failed to deserialize map's key to list of string object. Raw Json String: {}",
            keyData);
        throw e;
      }
    }

    List<String> values;
    if (PINOT_EMPTY_LIST.equals(valueData)) {
      values = new ArrayList<>();
    } else {
      try {
        values = objectMapper.readValue(valueData, listOfString);
      } catch (IOException e) {
        LOG.error(
            "Failed to deserialize map's value to list of string object. Raw Json String {}",
            valueData);
        throw e;
      }
    }

    if (keys.size() != values.size()) {
      LOG.warn(
          "Keys and Values data size does not match. Data will be return based on the kyes"
              + "Keys Size: {},  Values Size:  {}",
          keys.size(),
          values.size());
      // todo: make this debug once in production
      LOG.info("Keys: {}, \n Values:{}", keys, values);
    }

    // If the size does not match, the key is driving the map data. Any excessive values
    // will be dropped
    for (int idx = 0; idx < keys.size(); idx++) {
      if (idx < values.size()) {
        map.put(keys.get(idx), values.get(idx));
      } else {
        // to handle unbalanced size
        map.put(keys.get(idx), null);
      }
    }

    try {
      return objectMapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      LOG.error("Unable to write the merged map as json. Raw Data: {}", map);
      throw e;
    }
  }
}
