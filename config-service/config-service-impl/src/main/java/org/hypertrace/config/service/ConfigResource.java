package org.hypertrace.config.service;

import lombok.Value;

/**
 * Identifies the configuration resource which you want to deal with. A single config resource can
 * have multiple versions of config values associated with it.
 */
@Value
public class ConfigResource {

  String resourceName;
  String resourceNamespace;
  String tenantId;
  String context;
}
