package org.hypertrace.core.bootstrapper.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Objects;
import org.hypertrace.core.documentstore.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigBootstrapStatus implements Document {
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBootstrapStatus.class);

  private int version;
  private String name;
  private String fileChecksum;
  private Status status;
  private String rollbackConfig;

  public ConfigBootstrapStatus() {}

  public ConfigBootstrapStatus(
      int version, String name, String fileChecksum, Status status, String rollbackConfig) {
    this.version = version;
    this.name = name;
    this.fileChecksum = fileChecksum;
    this.status = status;
    this.rollbackConfig = rollbackConfig;
  }

  public static ConfigBootstrapStatus fromJson(String json) throws IOException {
    return OBJECT_MAPPER.readValue(json, ConfigBootstrapStatus.class);
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFileChecksum() {
    return fileChecksum;
  }

  public void setFileChecksum(String fileChecksum) {
    this.fileChecksum = fileChecksum;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getRollbackConfig() {
    return rollbackConfig;
  }

  public void setRollbackConfig(String rollbackConfig) {
    this.rollbackConfig = rollbackConfig;
  }

  @Override
  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      LOGGER.error("Error in converting {} to json", this);
      throw new RuntimeException("Error in converting ConfigBootstrapStatus to json");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConfigBootstrapStatus that = (ConfigBootstrapStatus) o;
    return version == that.version
        && Objects.equals(name, that.name)
        && Objects.equals(fileChecksum, that.fileChecksum)
        && status == that.status
        && Objects.equals(rollbackConfig, that.rollbackConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, name, fileChecksum, status, rollbackConfig);
  }

  public enum Status {
    SUCCEEDED,
    ROLLED_BACK
  }
}
