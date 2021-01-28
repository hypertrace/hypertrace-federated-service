package org.hypertrace.core.query.service.pinot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.apache.pinot.client.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class PinotResultAnalyzerTest {
  // Attribute 1
  private static final String LOGICAL_COL1 = "COL1";
  private static final String PHYS_COL1 = "PHYS_COL1";
  private static final String VAL_COL1 = "COL1_VAL";

  // Attribute 2
  private static final String LOGICAL_COL2 = "COL2";
  private static final String PHYS_COL2 = "PHYS_COL2";
  private static final String VAL_COL2 = "COL2_VAL";

  // Map Attribute 1
  private static final String LOGICAL_MAP_NAME1 = "MAP_COL1";
  private static final String MAP1_KEY_NAME = "MAP_COL1__KEYS";
  private static final String MAP1_VAL_NAME = "MAP_COL1__VALUES";
  private static final String MAP1_KEY_VAL = "[\"Content-Type\"]";
  private static final String MAP1_VAL_VAL = "[\"application/json\"]";

  // Map Attribute 3
  private static final String LOGICAL_MAP_NAME2 = "MAP_COL2";
  private static final String MAP2_KEY_NAME = "MAP_COL2__KEYS";
  private static final String MAP2_VAL_NAME = "MAP_COL2__VALUES";
  private static final String MAP2_KEY_VAL = "[\"Amazing\"]";
  private static final String MAP2_VAL_VAL = "[\"@TestOrg\"]";

  @Mock private ResultSet resultSet;
  @Mock private ViewDefinition viewDefinition;

  private PinotResultAnalyzer target;
  private List<String> resultSetColumnNames;
  private PinotMapConverter pinotMapConverter;

  @BeforeEach
  public void setup() {
    viewDefinition = mock(ViewDefinition.class);
    resultSet = mock(ResultSet.class);

    pinotMapConverter = new PinotMapConverter();
    Map<String, List<String>> viewDefinitionMap = ImmutableMap.<String, List<String>>builder()
        .put(LOGICAL_COL1, Lists.newArrayList(PHYS_COL1))
        .put(LOGICAL_COL2, Lists.newArrayList(PHYS_COL2))
        .put(LOGICAL_MAP_NAME1, Lists.newArrayList(MAP1_KEY_NAME, MAP1_VAL_NAME))
        .put(LOGICAL_MAP_NAME2, Lists.newArrayList(MAP2_KEY_NAME, MAP2_VAL_NAME))
        .build();
    viewDefinitionMap.forEach(
        (k, v) -> {
          when(viewDefinition.getPhysicalColumnNames(k)).thenReturn(v);
          if (v.size() > 1) {
            when(viewDefinition.isMap(k)).thenReturn(true);
          } else {
            when(viewDefinition.isMap(k)).thenReturn(false);
          }
        });
    when(viewDefinition.getKeyColumnNameForMap(LOGICAL_MAP_NAME1)).thenReturn(MAP1_KEY_NAME);
    when(viewDefinition.getValueColumnNameForMap(LOGICAL_MAP_NAME1)).thenReturn(MAP1_VAL_NAME);
    when(viewDefinition.getKeyColumnNameForMap(LOGICAL_MAP_NAME2)).thenReturn(MAP2_KEY_NAME);
    when(viewDefinition.getValueColumnNameForMap(LOGICAL_MAP_NAME2)).thenReturn(MAP2_VAL_NAME);

    LinkedHashSet<String> selectedAttributes = new LinkedHashSet<>(
        ImmutableList.<String>builder()
            .add(LOGICAL_COL1)
            .add(LOGICAL_MAP_NAME1)
            .add(LOGICAL_COL2)
            .add(LOGICAL_MAP_NAME2)
            .build());
    resultSetColumnNames =
        Lists.newArrayList(
            PHYS_COL1, MAP1_KEY_NAME, MAP1_VAL_NAME, PHYS_COL2, MAP2_KEY_NAME, MAP2_VAL_NAME);

    List<String> resultSetColumnValues = Lists.newArrayList(
        VAL_COL1, MAP1_KEY_VAL, MAP1_VAL_VAL, VAL_COL2, MAP2_KEY_VAL, MAP2_VAL_VAL);

    mockResultSet(resultSetColumnNames, resultSetColumnValues);
    target = PinotResultAnalyzer.create(resultSet, selectedAttributes, viewDefinition);
  }

  @Test
  public void test_create_validInputWithMap_shouldFindIndexCorrectly() {
    // assert index for non-map attributes
    assertEquals(
        findIndexInResultSet(resultSetColumnNames, PHYS_COL1),
        target.getPhysicalColumnIndex(LOGICAL_COL1));
    assertEquals(
        findIndexInResultSet(resultSetColumnNames, PHYS_COL2),
        target.getPhysicalColumnIndex(LOGICAL_COL2));

    // assert index for map attributes
    assertEquals(
        findIndexInResultSet(resultSetColumnNames, MAP1_KEY_NAME),
        target.getMapKeyIndex(LOGICAL_MAP_NAME1));
    assertEquals(
        findIndexInResultSet(resultSetColumnNames, MAP2_KEY_NAME),
        target.getMapKeyIndex(LOGICAL_MAP_NAME2));

    assertEquals(
        findIndexInResultSet(resultSetColumnNames, MAP1_VAL_NAME),
        target.getMapValueIndex(LOGICAL_MAP_NAME1));
    assertEquals(
        findIndexInResultSet(resultSetColumnNames, MAP2_VAL_NAME),
        target.getMapValueIndex(LOGICAL_MAP_NAME2));
  }

  @Test
  public void test_getDataFromRow_validInputWithTwoMaps_ShouldGetData() throws IOException {
    assertEquals(VAL_COL1, target.getDataFromRow(0, LOGICAL_COL1));
    assertEquals(VAL_COL2, target.getDataFromRow(0, LOGICAL_COL2));
    assertEquals(
        pinotMapConverter.merge(MAP1_KEY_VAL, MAP1_VAL_VAL),
        target.getDataFromRow(0, LOGICAL_MAP_NAME1));
    assertEquals(
        pinotMapConverter.merge(MAP2_KEY_VAL, MAP2_VAL_VAL),
        target.getDataFromRow(0, LOGICAL_MAP_NAME2));
  }

  private Integer findIndexInResultSet(List<String> resultSetColumns, String name) {
    for (int idx = 0; idx < resultSetColumns.size(); idx++) {
      if (name.equalsIgnoreCase(resultSetColumns.get(idx))) {
        return idx;
      }
    }
    return null;
  }

  private void mockResultSet(
      List<String> resultSetColumnNames, List<String> resultSetColumnValues) {
    when(resultSet.getColumnCount()).thenReturn(resultSetColumnNames.size());
    for (int idx = 0; idx < resultSetColumnNames.size(); idx++) {
      when(resultSet.getColumnName(idx)).thenReturn(resultSetColumnNames.get(idx));
      when(resultSet.getString(0, idx)).thenReturn(resultSetColumnValues.get(idx));
    }
  }
}
