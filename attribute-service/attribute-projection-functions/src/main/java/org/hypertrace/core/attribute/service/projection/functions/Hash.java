package org.hypertrace.core.attribute.service.projection.functions;

import static java.util.Objects.nonNull;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.creator.rfc4122.NameBasedSha1UuidCreator;
import java.util.UUID;
import javax.annotation.Nullable;

public class Hash {

  /**
   * Unique, randomly generated namespace that should never be used directly. Changing this value
   * would change any existing projections containing a hash and orphan any data persisted against
   * that such as stored entities.
   */
  private static final NameBasedSha1UuidCreator HASHER =
      UuidCreator.getNameBasedSha1Creator()
          .withNamespace(UUID.fromString("5088c92d-5e9c-43f4-a35b-2589474d5642"));

  @Nullable
  public static String hash(@Nullable String value) {
    return nonNull(value) ? HASHER.create(value).toString() : null;
  }
}
