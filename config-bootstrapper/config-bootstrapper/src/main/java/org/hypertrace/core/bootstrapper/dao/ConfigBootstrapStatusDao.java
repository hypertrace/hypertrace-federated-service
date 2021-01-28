package org.hypertrace.core.bootstrapper.dao;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.hypertrace.core.bootstrapper.model.ConfigBootstrapStatus;
import org.hypertrace.core.bootstrapper.model.ConfigBootstrapStatusKey;
import org.hypertrace.core.documentstore.Datastore;
import org.hypertrace.core.documentstore.Document;
import org.hypertrace.core.documentstore.Filter;
import org.hypertrace.core.documentstore.Query;
import org.hypertrace.core.documentstore.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Data Access Object for querying, updating and deleting bootstrap status in the datastore */
public class ConfigBootstrapStatusDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBootstrapStatus.class);
  private static final String ID = "_id";
  private static final String VERSION = "version";
  public static final String CONFIG_BOOTSTRAPPER_COLLECTION = "config_bootstrap_status";

  private final Collection configBootstrapStatusCollection;

  public ConfigBootstrapStatusDao(Datastore datastore) {
    configBootstrapStatusCollection = datastore.getCollection(CONFIG_BOOTSTRAPPER_COLLECTION);
  }

  public ConfigBootstrapStatus getConfigBootstrapStatus(ConfigBootstrapStatusKey key) {
    Query query = new Query();
    query.setFilter(new Filter(Filter.Op.EQ, ID, key.toString()));
    Iterator<Document> docs = configBootstrapStatusCollection.search(query);
    if (docs.hasNext()) {
      Document doc = docs.next();
      try {
        return ConfigBootstrapStatus.fromJson(doc.toJson());
      } catch (IOException ex) {
        LOGGER.error(String.format("Error creating ConfigBootstrapStatus from doc:%s", doc), ex);
        return null;
      }
    }
    return null;
  }

  public boolean upsertConfigBootstrapStatus(ConfigBootstrapStatus configBootstrapStatus) {
    try {
      configBootstrapStatusCollection.upsert(
          new ConfigBootstrapStatusKey(
              configBootstrapStatus.getVersion(), configBootstrapStatus.getName()),
          configBootstrapStatus);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public boolean deleteConfigBootstrapStatus(ConfigBootstrapStatusKey key) {
    return configBootstrapStatusCollection.delete(key);
  }

  public List<ConfigBootstrapStatus> getConfigBootstrapStatusGreaterThan(int version) {
    Query query = new Query();
    query.setFilter(new Filter(Filter.Op.GT, VERSION, version));
    Iterator<Document> docs = configBootstrapStatusCollection.search(query);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(docs, 0), false)
        .map(
            document -> {
              try {
                return ConfigBootstrapStatus.fromJson(document.toJson());
              } catch (IOException ex) {
                LOGGER.error("Error creating ConfigBootstrapStatus from doc:{}", document);
                return null;
              }
            })
        .filter(Objects::nonNull)
        // sort from higher version to lower version
        .sorted(
            (c1, c2) ->
                ConfigBootstrapStatusKey.from(c2).compareTo(ConfigBootstrapStatusKey.from(c1)))
        .collect(Collectors.toList());
  }
}
