package org.hypertrace.core.query.service.pinot;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.pinot.client.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Discovers the map attributes indexes from Pinot Result Set */
class PinotResultAnalyzer {
  private static final Logger LOG = LoggerFactory.getLogger(PinotResultAnalyzer.class);

  /* Stores the Map Attributes logical name to Physical Names */
  private final Map<String, Integer> mapLogicalNameToKeyIndex;
  private final Map<String, Integer> mapLogicalNameToValueIndex;

  /* Stores the Non-Map Attributes logical name to Physical Name index */
  private final Map<String, Integer> logicalNameToPhysicalNameIndex;
  private final ResultSet resultSet;
  private final ViewDefinition viewDefinition;
  private final Map<String, RateLimiter> attributeLogRateLimitter;
  private final PinotMapConverter pinotMapConverter;

  PinotResultAnalyzer(
      ResultSet resultSet,
      LinkedHashSet<String> selectedAttributes,
      ViewDefinition viewDefinition,
      Map<String, Integer> mapLogicalNameToKeyIndex,
      Map<String, Integer> mapLogicalNameToValueIndex,
      Map<String, Integer> logicalNameToPhysicalNameIndex) {
    this.mapLogicalNameToKeyIndex = mapLogicalNameToKeyIndex;
    this.mapLogicalNameToValueIndex = mapLogicalNameToValueIndex;
    this.logicalNameToPhysicalNameIndex = logicalNameToPhysicalNameIndex;
    this.resultSet = resultSet;
    this.viewDefinition = viewDefinition;
    this.attributeLogRateLimitter = new HashMap<>();
    selectedAttributes.forEach(e -> attributeLogRateLimitter.put(e, RateLimiter.create(0.5)));
    this.pinotMapConverter = new PinotMapConverter();
  }

  /** For each selected attributes build the map of logical name to result index. */
  static PinotResultAnalyzer create(
      ResultSet resultSet,
      LinkedHashSet<String> selectedAttributes,
      ViewDefinition viewDefinition) {
    Map<String, Integer> mapLogicalNameToKeyIndex = new HashMap<>();
    Map<String, Integer> mapLogicalNameToValueIndex = new HashMap<>();
    Map<String, Integer> logicalNameToPhysicalNameIndex = new HashMap<>();

    for (String logicalName : selectedAttributes) {
      if (viewDefinition.isMap(logicalName)) {
        String keyPhysicalName = viewDefinition.getKeyColumnNameForMap(logicalName);
        String valuePhysicalName = viewDefinition.getValueColumnNameForMap(logicalName);
        for (int colIndex = 0; colIndex < resultSet.getColumnCount(); colIndex++) {
          String physName = resultSet.getColumnName(colIndex);
          if (physName.equalsIgnoreCase(keyPhysicalName)) {
            mapLogicalNameToKeyIndex.put(logicalName, colIndex);
          } else if (physName.equalsIgnoreCase(valuePhysicalName)) {
            mapLogicalNameToValueIndex.put(logicalName, colIndex);
          }
        }
      } else {
        List<String> names = viewDefinition.getPhysicalColumnNames(logicalName);
        Preconditions.checkArgument(names.size() == 1);
        for (int colIndex = 0; colIndex < resultSet.getColumnCount(); colIndex++) {
          String physName = resultSet.getColumnName(colIndex);
          if (physName.equalsIgnoreCase(names.get(0))) {
            logicalNameToPhysicalNameIndex.put(logicalName, colIndex);
            break;
          }
        }
      }
    }
    LOG.info("Map LogicalName to Key Index: {} ", mapLogicalNameToKeyIndex);
    LOG.info("Map LogicalName to Value Index: {}", mapLogicalNameToValueIndex);
    LOG.info("Attributes to Index: {}", logicalNameToPhysicalNameIndex);
    return new PinotResultAnalyzer(
        resultSet,
        selectedAttributes,
        viewDefinition,
        mapLogicalNameToKeyIndex,
        mapLogicalNameToValueIndex,
        logicalNameToPhysicalNameIndex);
  }

  @VisibleForTesting
  Integer getMapKeyIndex(String logicalName) {
    return mapLogicalNameToKeyIndex.get(logicalName);
  }

  @VisibleForTesting
  Integer getMapValueIndex(String logicalName) {
    return mapLogicalNameToValueIndex.get(logicalName);
  }

  @VisibleForTesting
  Integer getPhysicalColumnIndex(String logicalName) {
    return logicalNameToPhysicalNameIndex.get(logicalName);
  }

  /**
   * Gets the data from Result Set Row, will never null
   *
   * @throws IllegalStateException if index is missing for merging or there's an issue with the data
   *     format in Pinot
   * @return merged map data if in correct format. Will never return null
   */
  @Nonnull
  String getDataFromRow(int rowIndex, String logicalName) {

    String result;
    if (viewDefinition.isMap(logicalName)) {
      Integer keyIndex = getMapKeyIndex(logicalName);
      if (keyIndex == null) {
        LOG.info("Map LogicalName to Key Index: {} ", mapLogicalNameToKeyIndex);
        LOG.info("Attributes to Index: {}", logicalNameToPhysicalNameIndex);
        throw new IllegalStateException(
            "Unable to find the key index to attribute: " + logicalName);
      }
      String keyData = resultSet.getString(rowIndex, keyIndex);

      String valueData = "";
      Integer valueIndex = getMapValueIndex(logicalName);
      if (valueIndex == null) {
        if (attributeLogRateLimitter.get(logicalName).tryAcquire()) {
          LOG.error("Unable to find the map value column index for Attribute: {}.", logicalName);
          LOG.info("Map LogicalName to Value Index: {} ", mapLogicalNameToValueIndex);
          LOG.info("Attributes to Index: {}", logicalNameToPhysicalNameIndex);
        }
      } else {
        valueData = resultSet.getString(rowIndex, valueIndex);
      }
      try {
        result = pinotMapConverter.merge(keyData, valueData);
      } catch (IOException e) {
        throw new IllegalStateException(
            "Unable to merge the map data for attribute " + logicalName, e);
      }
    } else {
      Integer colIndex = getPhysicalColumnIndex(logicalName);
      result = resultSet.getString(rowIndex, colIndex);
    }
    return result;
  }
}
