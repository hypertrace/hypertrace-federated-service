package org.hypertrace.core.query.service.pinot;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import org.apache.commons.codec.binary.Hex;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.ConnectionFactory;
import org.apache.pinot.client.PreparedStatement;
import org.apache.pinot.client.Request;
import org.apache.pinot.client.ResultSetGroup;

/*
 * Factory to create PinotClient based on given zookeeper path.
 */
public class PinotClientFactory {

  private static final org.slf4j.Logger LOG =
      org.slf4j.LoggerFactory.getLogger(PinotClientFactory.class);
  // Singleton instance
  private static final PinotClientFactory INSTANCE = new PinotClientFactory();

  private final ConcurrentHashMap<String, PinotClient> clientMap = new ConcurrentHashMap<>();

  private PinotClientFactory() {
  }

  // Create a Pinot Client.
  public static PinotClient createPinotClient(String pinotCluster, String pathType, String path) {
    if (!get().containsClient(pinotCluster)) {
      synchronized (get()) {
        if (!get().containsClient(pinotCluster)) {
          get().addPinotClient(pinotCluster, new PinotClient(pathType, path));
        }
      }
    }
    return get().getPinotClient(pinotCluster);
  }

  public static PinotClientFactory get() {
    return INSTANCE;
  }

  private void addPinotClient(String cluster, PinotClient client) {
    this.clientMap.put(cluster, client);
  }

  public boolean containsClient(String clusterName) {
    return this.clientMap.containsKey(clusterName);
  }

  public PinotClient getPinotClient(String clusterName) {
    return this.clientMap.get(clusterName);
  }

  public static class PinotClient {

    private static final String SQL_FORMAT = "sql";

    private final Connection connection;

    @VisibleForTesting
    public PinotClient(Connection connection) {
      this.connection = connection;
    }

    private PinotClient(String pathType, String path) {
      switch (pathType.toLowerCase()) {
        case "zk":
        case "zookeeper":
          LOG.info("Trying to create a Pinot client connected to Zookeeper: {}", path);
          this.connection = ConnectionFactory.fromZookeeper(path);
          break;
        case "broker":
          LOG.info("Trying to create a Pinot client with default brokerlist: {}", path);
          this.connection = ConnectionFactory.fromHostList(path);
          break;
        default:
          throw new RuntimeException("Unsupported Pinot Client scheme: " + pathType);
      }
    }

    public ResultSetGroup executeQuery(String statement, Params params) {
      PreparedStatement preparedStatement = buildPreparedStatement(statement, params);
      return preparedStatement.execute();
    }

    public Future<ResultSetGroup> executeQueryAsync(String statement, Params params) {
      PreparedStatement preparedStatement = buildPreparedStatement(statement, params);
      return preparedStatement.executeAsync();
    }

    private PreparedStatement buildPreparedStatement(String statement, Params params) {
      Request request = new Request(SQL_FORMAT, statement);
      PreparedStatement preparedStatement = connection.prepareStatement(request);
      params.getStringParams().forEach(preparedStatement::setString);
      params.getIntegerParams().forEach(preparedStatement::setInt);
      params.getLongParams().forEach(preparedStatement::setLong);
      params.getDoubleParams().forEach(preparedStatement::setDouble);
      params.getFloatParams().forEach(preparedStatement::setFloat);
      params.getByteStringParams()
          .forEach((i, b) -> preparedStatement.setString(i, Hex.encodeHexString(b.toByteArray())));
      return preparedStatement;
    }
  }
}
