package org.hypertrace.config.service.store;

import lombok.Value;
import org.hypertrace.config.service.ConfigResource;
import org.hypertrace.core.documentstore.Key;

/**
 * Key for the {@link ConfigDocument} (used by {@link DocumentConfigStore}).
 */
@Value
public class ConfigDocumentKey implements Key {

  private static final String SEPARATOR = ":";

  ConfigResource configResource;
  long configVersion;

  @Override
  public String toString() {
    return String.join(SEPARATOR, configResource.getResourceName(),
        configResource.getResourceNamespace(), configResource.getTenantId(),
        configResource.getContext(), String.valueOf(configVersion));
  }
}
