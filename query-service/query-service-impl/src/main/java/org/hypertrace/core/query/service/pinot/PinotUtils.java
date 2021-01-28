package org.hypertrace.core.query.service.pinot;

public class PinotUtils {

  public static String getZkPath(String zkBasePath, String pinotClusterName) {
    return zkBasePath.endsWith("/")
        ? zkBasePath + pinotClusterName
        : zkBasePath + "/" + pinotClusterName;
  }

  public static String getMetricName(String name) {
    return name.replaceAll("[^a-zA-Z0-9_]", ".");
  }
}
