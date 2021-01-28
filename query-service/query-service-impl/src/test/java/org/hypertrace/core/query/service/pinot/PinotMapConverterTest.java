package org.hypertrace.core.query.service.pinot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PinotMapConverterTest {
  private static final String KEY1 = "KEY1";
  private static final String KEY2 = "KEY2";
  private static final String VAL1 = "VAL1";
  private static final String VAL2 = "VAL2";

  private List<String> validKeys;
  private List<String> validValues;
  private String validKeysJsonString;
  private String validValueJsonString;
  private String emptyMapJsonString;
  private String expectedValidMapString;
  private ObjectMapper objectMapper;
  private Map<String, String> expectedMap;
  private PinotMapConverter target;

  @BeforeEach
  public void setup() throws JsonProcessingException {
    objectMapper = new ObjectMapper();
    emptyMapJsonString = objectMapper.writeValueAsString(Collections.emptyMap());

    validKeys = Lists.newArrayList(KEY1, KEY2);
    validValues = Lists.newArrayList(VAL1, VAL2);
    validKeysJsonString = objectMapper.writeValueAsString(validKeys);
    validValueJsonString = objectMapper.writeValueAsString(validValues);

    expectedMap = new HashMap<>();
    expectedMap.put(KEY1, VAL1);
    expectedMap.put(KEY2, VAL2);
    expectedValidMapString = objectMapper.writeValueAsString(expectedMap);

    target = new PinotMapConverter();
  }

  @Test
  public void test_merge_nullKey_shouldThrowException() throws IOException {
    assertThrows(
        IOException.class,
        () -> {
          target.merge(null, "");
        });
  }

  @Test
  public void test_merge_emptyListStringKey_shouldReturnEmptyMap() throws IOException {
    assertEquals(emptyMapJsonString, target.merge("[]", "[]"));
  }

  @Test
  public void test_merge_PinotemptyList_shouldReturnEmptyMap() throws IOException {
    assertEquals(emptyMapJsonString, target.merge("[\"\"]", "[\"\"]"));
  }

  @Test
  public void test_merge_validKeyEmptyStringValue_shouldReturnKeyWithEmptyValue()
      throws IOException {
    String expected = objectMapper.writeValueAsString(expectedMap);
    assertEquals(expected, target.merge(validKeysJsonString, validValueJsonString));
  }

  @Test
  public void test_merge_largerKeysThanValues_shouldReturnBasedOnKeys() throws IOException {
    String newKey = "KEY3";
    expectedMap.put(newKey, null);
    validKeys.add(newKey);
    String largerKeysString = objectMapper.writeValueAsString(validKeys);
    String expectedMapString = objectMapper.writeValueAsString(expectedMap);
    assertEquals(expectedMapString, target.merge(largerKeysString, validValueJsonString));
  }

  @Test
  public void test_merge_largerValuesThanKeys_shouldReturnBasedOnKeys() throws IOException {
    String newValue = "VALUE3";
    validValues.add(newValue);
    String largerValuesString = objectMapper.writeValueAsString(validValues);
    assertEquals(expectedValidMapString, target.merge(validKeysJsonString, largerValuesString));
  }
}
