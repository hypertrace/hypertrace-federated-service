package org.hypertrace.config.service.store;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.protobuf.Value;
import com.typesafe.config.Config;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.hypertrace.config.service.ConfigResource;
import org.hypertrace.config.service.ConfigServiceUtils;
import org.hypertrace.config.service.v1.ContextSpecificConfig;
import org.hypertrace.core.documentstore.Collection;
import org.hypertrace.core.documentstore.Datastore;
import org.hypertrace.core.documentstore.DatastoreProvider;
import org.hypertrace.core.documentstore.Document;
import org.hypertrace.core.documentstore.Filter;
import org.hypertrace.core.documentstore.Key;
import org.hypertrace.core.documentstore.OrderBy;
import org.hypertrace.core.documentstore.Query;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import static org.hypertrace.config.service.ConfigServiceUtils.emptyValue;
import static org.hypertrace.config.service.store.ConfigDocument.CONTEXT_FIELD_NAME;
import static org.hypertrace.config.service.store.ConfigDocument.RESOURCE_FIELD_NAME;
import static org.hypertrace.config.service.store.ConfigDocument.RESOURCE_NAMESPACE_FIELD_NAME;
import static org.hypertrace.config.service.store.ConfigDocument.TENANT_ID_FIELD_NAME;
import static org.hypertrace.config.service.store.ConfigDocument.VERSION_FIELD_NAME;

/**
 * Document store which stores and serves the configurations.
 */
@Slf4j
public class DocumentConfigStore implements ConfigStore {

  static final String DOC_STORE_CONFIG_KEY = "document.store";
  static final String DATA_STORE_TYPE = "dataStoreType";
  static final String CONFIGURATIONS_COLLECTION = "configurations";

  private final LoadingCache<ConfigResource, Object> configResourceLocks =
      CacheBuilder.newBuilder()
          .expireAfterAccess(10, TimeUnit.MINUTES) // max lock time ever expected
          .build(CacheLoader.from(Object::new));

  private Datastore datastore;
  private Collection collection;

  @Override
  public void init(Config config) {
    datastore = initDataStore(config);
    this.collection = getOrCreateCollection(datastore);
  }

  private Datastore initDataStore(Config config) {
    Config docStoreConfig = config.getConfig(DOC_STORE_CONFIG_KEY);
    String dataStoreType = docStoreConfig.getString(DATA_STORE_TYPE);
    Config dataStoreConfig = docStoreConfig.getConfig(dataStoreType);
    return DatastoreProvider.getDatastore(dataStoreType, dataStoreConfig);
  }

  private Collection getOrCreateCollection(Datastore datastore) {
    if (!datastore.listCollections().contains(CONFIGURATIONS_COLLECTION)) {
      if (!datastore.createCollection(CONFIGURATIONS_COLLECTION, Collections.emptyMap())) {
        throw new RuntimeException(
            "Failed to create collection:" + CONFIGURATIONS_COLLECTION + " in document store");
      }
    }
    return datastore.getCollection(CONFIGURATIONS_COLLECTION);
  }

  @Override
  public long writeConfig(ConfigResource configResource, String userId, Value config)
      throws IOException {
    // Synchronization is required across different threads trying to write the latest config
    // for the same resource into the document store
    synchronized (configResourceLocks.getUnchecked(configResource)) {
      long configVersion = getLatestVersion(configResource) + 1;
      Key key = new ConfigDocumentKey(configResource, configVersion);
      Document document = new ConfigDocument(configResource.getResourceName(),
          configResource.getResourceNamespace(), configResource.getTenantId(),
          configResource.getContext(), configVersion, userId, config);
      collection.upsert(key, document);
      return configVersion;
    }
  }

  @Override
  public Value getConfig(ConfigResource configResource) throws IOException {
    Value config = emptyValue();
    Filter filter = getConfigResourceFilter(configResource)
        .and(Filter.eq(VERSION_FIELD_NAME, getLatestVersion(configResource)));
    Query query = new Query();
    query.setFilter(filter);
    Iterator<Document> documentIterator = collection.search(query);
    if (documentIterator.hasNext()) {
      String documentString = documentIterator.next().toJson();
      ConfigDocument configDocument = ConfigDocument.fromJson(documentString);
      config = configDocument.getConfig();
    }
    return config;
  }

  @Override
  public List<ContextSpecificConfig> getAllConfigs(String resourceName, String resourceNamespace,
      String tenantId) throws IOException {
    Filter filter = Filter.eq(RESOURCE_FIELD_NAME, resourceName)
        .and(Filter.eq(RESOURCE_NAMESPACE_FIELD_NAME, resourceNamespace))
        .and(Filter.eq(TENANT_ID_FIELD_NAME, tenantId));
    Query query = new Query();
    query.setFilter(filter);
    query.addOrderBy(new OrderBy(VERSION_FIELD_NAME, false));
    Iterator<Document> documentIterator = collection.search(query);
    List<ContextSpecificConfig> contextSpecificConfigList = new ArrayList<>();
    Set<String> seenContexts = new HashSet<>();
    while (documentIterator.hasNext()) {
      String documentString = documentIterator.next().toJson();
      ConfigDocument configDocument = ConfigDocument.fromJson(documentString);
      String context = configDocument.getContext();
      Value config = configDocument.getConfig();
      if (seenContexts.add(context) && !ConfigServiceUtils.isNull(config)) {
        contextSpecificConfigList
            .add(ContextSpecificConfig.newBuilder().setContext(context).setConfig(config).build());
      }
    }
    return contextSpecificConfigList;
  }

  @Override
  public boolean healthCheck() {
    return datastore.healthCheck();
  }

  private long getLatestVersion(ConfigResource configResource) throws IOException {
    Query query = new Query();
    query.setFilter(getConfigResourceFilter(configResource));
    query.addOrderBy(new OrderBy(VERSION_FIELD_NAME, false));
    query.setLimit(1);

    Iterator<Document> documentIterator = collection.search(query);
    if (documentIterator.hasNext()) {
      String documentString = documentIterator.next().toJson();
      ConfigDocument documentWithLatestVersion = ConfigDocument.fromJson(documentString);
      return documentWithLatestVersion.getConfigVersion();
    }
    return 0;
  }

  private Filter getConfigResourceFilter(ConfigResource configResource) {
    return Filter.eq(RESOURCE_FIELD_NAME, configResource.getResourceName())
        .and(Filter.eq(RESOURCE_NAMESPACE_FIELD_NAME, configResource.getResourceNamespace()))
        .and(Filter.eq(TENANT_ID_FIELD_NAME, configResource.getTenantId()))
        .and(Filter.eq(CONTEXT_FIELD_NAME, configResource.getContext()));
  }
}
