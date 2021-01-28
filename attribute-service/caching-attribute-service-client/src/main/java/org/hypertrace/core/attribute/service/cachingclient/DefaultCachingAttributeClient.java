package org.hypertrace.core.attribute.service.cachingclient;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeMetadataFilter;
import org.hypertrace.core.attribute.service.v1.AttributeServiceGrpc;
import org.hypertrace.core.attribute.service.v1.AttributeServiceGrpc.AttributeServiceStub;

class DefaultCachingAttributeClient implements CachingAttributeClient {

  private final AttributeServiceStub attributeServiceClient;
  private final LoadingCache<
          AttributeCacheContextKey, Single<Table<String, String, AttributeMetadata>>>
      cache;
  private final Cache<String, AttributeScopeAndKey> scopeAndKeyLookup;
  private final AttributeMetadataFilter attributeFilter;

  DefaultCachingAttributeClient(
      @Nonnull Channel channel,
      @Nonnull CallCredentials credentials,
      int maxCacheContexts,
      @Nonnull Duration cacheExpiration,
      @Nonnull AttributeMetadataFilter attributeFilter) {

    this.attributeFilter = attributeFilter;
    this.attributeServiceClient =
        AttributeServiceGrpc.newStub(channel).withCallCredentials(credentials);
    this.cache =
        CacheBuilder.newBuilder()
            .maximumSize(maxCacheContexts)
            .expireAfterWrite(cacheExpiration)
            .build(CacheLoader.from(this::loadTable));
    this.scopeAndKeyLookup = CacheBuilder.newBuilder().expireAfterWrite(cacheExpiration).build();
  }

  @Override
  public Single<AttributeMetadata> get(String scope, String key) {
    return this.getOrInvalidate(AttributeCacheContextKey.forCurrentContext())
        .mapOptional(table -> Optional.ofNullable(table.get(scope, key)))
        .switchIfEmpty(Single.error(this.buildErrorForMissingAttribute(scope, key)));
  }

  @Override
  public Single<AttributeMetadata> get(String attributeId) {
    return this.getOrInvalidate(AttributeCacheContextKey.forCurrentContext())
        .mapOptional(
            table ->
                Optional.ofNullable(this.scopeAndKeyLookup.getIfPresent(attributeId))
                    .map(scopeAndKey -> table.get(scopeAndKey.scope, scopeAndKey.key)))
        .switchIfEmpty(Single.error(this.buildErrorForMissingAttribute(attributeId)));
  }

  @Override
  public Single<List<AttributeMetadata>> getAllInScope(String scope) {
    return this.getOrInvalidate(AttributeCacheContextKey.forCurrentContext())
        .map(table -> List.copyOf(table.row(scope).values()));
  }

  @Override
  public Single<List<AttributeMetadata>> getAll() {
    return this.getOrInvalidate(AttributeCacheContextKey.forCurrentContext())
        .map(table -> List.copyOf(table.values()));
  }

  private Single<Table<String, String, AttributeMetadata>> loadTable(AttributeCacheContextKey key) {
    return key.getExecutionContext().<AttributeMetadata>stream(
            streamObserver ->
                this.attributeServiceClient.findAttributes(this.attributeFilter, streamObserver))
        .doOnNext(this::loadScopeAndKeyCache)
        .toList()
        .map(this::buildTable)
        .cache();
  }

  private Table<String, String, AttributeMetadata> buildTable(List<AttributeMetadata> attributes) {
    return attributes.stream()
        .collect(
            ImmutableTable.toImmutableTable(
                AttributeMetadata::getScopeString,
                AttributeMetadata::getKey,
                Function.identity()));
  }

  private Single<Table<String, String, AttributeMetadata>> getOrInvalidate(
      AttributeCacheContextKey key) {
    return this.cache.getUnchecked(key).doOnError(x -> this.cache.invalidate(key));
  }

  private NoSuchElementException buildErrorForMissingAttribute(String scope, String key) {
    return new NoSuchElementException(
        String.format("No attribute available for scope '%s' and key '%s'", scope, key));
  }

  private NoSuchElementException buildErrorForMissingAttribute(String attributeId) {
    return new NoSuchElementException(
        String.format("No attribute available for id '%s'", attributeId));
  }

  private void loadScopeAndKeyCache(AttributeMetadata attributeMetadata) {
    this.scopeAndKeyLookup.put(
        attributeMetadata.getId(),
        new AttributeScopeAndKey(attributeMetadata.getScopeString(), attributeMetadata.getKey()));
  }

  private static final class AttributeScopeAndKey {
    private final String scope;
    private final String key;

    private AttributeScopeAndKey(String scope, String key) {
      this.scope = scope;
      this.key = key;
    }
  }
}
