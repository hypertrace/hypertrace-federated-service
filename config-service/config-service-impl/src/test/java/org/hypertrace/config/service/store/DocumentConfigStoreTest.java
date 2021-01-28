package org.hypertrace.config.service.store;

import static org.hypertrace.config.service.ConfigServiceUtils.DEFAULT_CONTEXT;
import static org.hypertrace.config.service.TestUtils.CONTEXT1;
import static org.hypertrace.config.service.TestUtils.RESOURCE_NAME;
import static org.hypertrace.config.service.TestUtils.RESOURCE_NAMESPACE;
import static org.hypertrace.config.service.TestUtils.TENANT_ID;
import static org.hypertrace.config.service.TestUtils.getConfig1;
import static org.hypertrace.config.service.TestUtils.getConfig2;
import static org.hypertrace.config.service.TestUtils.getConfigResource;
import static org.hypertrace.config.service.store.DocumentConfigStore.CONFIGURATIONS_COLLECTION;
import static org.hypertrace.config.service.store.DocumentConfigStore.DATA_STORE_TYPE;
import static org.hypertrace.config.service.store.DocumentConfigStore.DOC_STORE_CONFIG_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.protobuf.Value;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hypertrace.config.service.ConfigResource;
import org.hypertrace.config.service.v1.ContextSpecificConfig;
import org.hypertrace.core.documentstore.Collection;
import org.hypertrace.core.documentstore.Datastore;
import org.hypertrace.core.documentstore.DatastoreProvider;
import org.hypertrace.core.documentstore.Document;
import org.hypertrace.core.documentstore.Key;
import org.hypertrace.core.documentstore.Query;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DocumentConfigStoreTest {

  private static final long CONFIG_VERSION = 5;
  private static final String USER_ID = "user1";
  private static DocumentConfigStore configStore = new DocumentConfigStore();
  private static Value config1 = getConfig1();
  private static Value config2 = getConfig2();
  private static ConfigResource configResource = getConfigResource();
  private static Collection collection;

  @BeforeAll
  static void init() {
    collection = mock(Collection.class);
    String datastoreType = "MockDatastore";
    DatastoreProvider.register(datastoreType, MockDatastore.class);
    Map<String, Object> dataStoreConfig = Map.of(DATA_STORE_TYPE, datastoreType,
        datastoreType, Map.of());
    Map<String, Object> configMap = Map.of(DOC_STORE_CONFIG_KEY, dataStoreConfig);
    Config storeConfig = ConfigFactory.parseMap(configMap);
    configStore.init(storeConfig);
  }

  @Test
  void writeConfig() throws IOException {
    List<Document> documentList = Collections
        .singletonList(getConfigDocument(DEFAULT_CONTEXT, CONFIG_VERSION, config1));
    when(collection.search(any(Query.class))).thenReturn(documentList.iterator());

    configStore.writeConfig(configResource, USER_ID, config1);

    ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
    ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
    verify(collection, times(1)).upsert(keyCaptor.capture(), documentCaptor.capture());

    Key key = keyCaptor.getValue();
    Document document = documentCaptor.getValue();
    long newVersion = CONFIG_VERSION + 1;
    assertEquals(new ConfigDocumentKey(configResource, newVersion), key);
    assertEquals(getConfigDocument(DEFAULT_CONTEXT, newVersion, config1), document);
  }

  @Test
  void getConfig() throws IOException {
    List<Document> documentList = Collections
        .singletonList(getConfigDocument(DEFAULT_CONTEXT, CONFIG_VERSION, config1));
    when(collection.search(any(Query.class)))
        .thenReturn(documentList.iterator(), documentList.iterator());

    Value config = configStore.getConfig(configResource);
    assertEquals(config1, config);
  }

  @Test
  void getAllConfigs() throws IOException {
    List<Document> documentList = List
        .of(getConfigDocument(DEFAULT_CONTEXT, CONFIG_VERSION, config1),
            getConfigDocument(DEFAULT_CONTEXT, CONFIG_VERSION - 1, config2),
            getConfigDocument(CONTEXT1, 1L, config2));
    when(collection.search(any(Query.class))).thenReturn(documentList.iterator());

    List<ContextSpecificConfig> contextSpecificConfigList = configStore
        .getAllConfigs(RESOURCE_NAME, RESOURCE_NAMESPACE, TENANT_ID);
    assertEquals(2, contextSpecificConfigList.size());
    assertEquals(
        ContextSpecificConfig.newBuilder().setContext(DEFAULT_CONTEXT).setConfig(config1).build(),
        contextSpecificConfigList.get(0));
    assertEquals(
        ContextSpecificConfig.newBuilder().setContext(CONTEXT1).setConfig(config2).build(),
        contextSpecificConfigList.get(1));
  }

  private static Document getConfigDocument(String context, long version, Value config) {
    return new ConfigDocument(RESOURCE_NAME, RESOURCE_NAMESPACE,
        TENANT_ID, context, version, USER_ID, config);
  }

  public static class MockDatastore implements Datastore {

    @Override
    public Set<String> listCollections() {
      return Collections.singleton(CONFIGURATIONS_COLLECTION);
    }

    @Override
    public Collection getCollection(String s) {
      return collection;
    }

    // default implementation for other methods as they are unused

    @Override
    public boolean init(Config config) {
      return false;
    }

    @Override
    public boolean createCollection(String s, Map<String, String> map) {
      return false;
    }

    @Override
    public boolean deleteCollection(String s) {
      return false;
    }

    @Override
    public boolean healthCheck() {
      return false;
    }
  }
}