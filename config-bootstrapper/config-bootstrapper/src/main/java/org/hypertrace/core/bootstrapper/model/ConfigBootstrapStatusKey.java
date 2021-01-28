package org.hypertrace.core.bootstrapper.model;

import java.util.Objects;
import org.hypertrace.core.documentstore.Key;

public class ConfigBootstrapStatusKey implements Key, Comparable<ConfigBootstrapStatusKey> {
  private int version;
  private String name;

  public ConfigBootstrapStatusKey(int version, String name) {
    this.version = version;
    this.name = name;
  }

  public static ConfigBootstrapStatusKey from(ConfigBootstrapStatus configBootstrapStatus) {
    return new ConfigBootstrapStatusKey(
        configBootstrapStatus.getVersion(), configBootstrapStatus.getName());
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConfigBootstrapStatusKey that = (ConfigBootstrapStatusKey) o;
    return version == that.version && name.equals(that.name);
  }

  @Override
  public String toString() {
    return String.format("%s:%s", version, name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, name);
  }

  @Override
  public int compareTo(ConfigBootstrapStatusKey other) {
    int versionCompare = Integer.compare(version, other.version);
    return versionCompare != 0 ? versionCompare : name.compareTo(other.getName());
  }
}
