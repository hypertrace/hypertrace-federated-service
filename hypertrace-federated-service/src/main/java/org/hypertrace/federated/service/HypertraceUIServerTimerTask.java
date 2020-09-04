package org.hypertrace.federated.service;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import io.grpc.Deadline;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.hypertrace.core.grpcutils.client.GrpcClientRequestContextUtil;
import org.hypertrace.core.grpcutils.client.RequestContextClientCallCredsProviderFactory;
import org.hypertrace.gateway.service.GatewayServiceGrpc;
import org.hypertrace.gateway.service.GatewayServiceGrpc.GatewayServiceBlockingStub;
import org.hypertrace.gateway.service.common.util.QueryExpressionUtil;
import org.hypertrace.gateway.service.v1.span.SpansRequest;
import org.hypertrace.gateway.service.v1.span.SpansResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper TimerTask for checking health of dependency data services before starting UI server
 */
public class HypertraceUIServerTimerTask extends TimerTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(HypertraceUIServerTimerTask.class);

  private static final String RETRIES_CONFIG = "hypertraceUI.init.waittime.retries";
  private static final int DEFAULT_RETRIES = 10;
  private static final String TIMEOUT_CONFIG = "hypertraceUI.init.waittime.timeout";
  private static final int DEFAULT_TIMEOUT = 2;
  private static final String INTERVAL = "hypertraceUI.init.waittime.interval";
  private static final int DEFAULT_INTERVAL = 5;
  private static final String START_PERIOD = "hypertraceUI.init.waittime.start_period";
  private static final int DEFAULT_START_PERIOD = 20;
  private static final String PINOT_SERVER_HOST = "pinot.server_host";
  private static final String DEFAULT_PINOT_SERVER_HOST = "pinot-controller";
  private static final String PINOT_SERVER_PORT = "pinot.server_port";
  private static final int DEFAULT_PINOT_SERVER_PORT = 8097;
  private static final String PINOT_CONTROLLER_PORT = "pinot.controller_port";
  private static final int DEFAULT_PINOT_CONTROLLER_PORT = 9000;

  private final HypertraceUIServer uiServer;
  private final GatewayServiceBlockingStub client;
  private int numRetries;
  private int maxRetries;
  private int timeout;
  private long startTime;
  private long interval;
  private long startPeriod;
  private String defaultTenant;
  private String pinotSeverHost;
  private int pinotServerPort;
  private int pinotControllerPort;


  public HypertraceUIServerTimerTask(Config appConfig, HypertraceUIServer uiServer, String defaultTenant) {
    maxRetries = appConfig.hasPath(RETRIES_CONFIG) ? appConfig.getInt(RETRIES_CONFIG) : DEFAULT_RETRIES;
    timeout = appConfig.hasPath(TIMEOUT_CONFIG) ? appConfig.getInt(TIMEOUT_CONFIG) : DEFAULT_TIMEOUT;
    interval = appConfig.hasPath(INTERVAL) ? appConfig.getInt(INTERVAL) : DEFAULT_INTERVAL;
    startPeriod = appConfig.hasPath(START_PERIOD) ? appConfig.getInt(START_PERIOD) : DEFAULT_START_PERIOD;

    pinotSeverHost = appConfig.hasPath(PINOT_SERVER_HOST) ?
            appConfig.getString(PINOT_SERVER_HOST) : DEFAULT_PINOT_SERVER_HOST;
    pinotServerPort = appConfig.hasPath(PINOT_SERVER_PORT) ?
            appConfig.getInt(PINOT_SERVER_PORT) : DEFAULT_PINOT_SERVER_PORT;
    pinotControllerPort = appConfig.hasPath(PINOT_CONTROLLER_PORT) ?
            appConfig.getInt(PINOT_CONTROLLER_PORT) : DEFAULT_PINOT_CONTROLLER_PORT;

    this.uiServer = uiServer;
    this.numRetries = 0;
    this.defaultTenant = defaultTenant;
    this.startTime = Instant.now().toEpochMilli();

    client = GatewayServiceGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(
            "localhost", appConfig.getInt("service.port")).usePlaintext().build())
            .withCallCredentials(RequestContextClientCallCredsProviderFactory
                    .getClientCallCredsProvider().get());
  }

  public long getStartPeriod() {
    return startPeriod;
  }

  public long getInterval() {
    return interval;
  }

  @Override
  public void run() {
    try {
      if (numRetries >= maxRetries) {
        cancel();
        LOGGER.info(String.format("Max out attempts [%s] in checking bootstrapping status. Manually check " +
                "the status of data service [pinot].", numRetries));
        uiServer.start();
        return;
      }

      if (executePinotHealthCheck() && executeBrokerRegistrationCheck() && executeHealthCheck()) {
        cancel();
        LOGGER.info(String.format("Stack is up after [%s] attempts, and duration [%s] in millis.",
                numRetries, Instant.now().toEpochMilli() - startTime));
        uiServer.start();
        return;
      }

      LOGGER.warn(String.format("Finished an attempt [%s] in checking for bootstrapping status. " +
              "It seems dependent data service [pinot] is not yet up. " +
              "will retry after [%s] seconds", numRetries, interval));

    } catch (Exception ex) {
      LOGGER.warn(String.format("Finished an attempt [%s] in checking for bootstrapping status. " +
              "It seems dependent data service [pinot] is not yet up. " +
              "will retry after [%s] seconds", numRetries, interval));
    } finally {
      numRetries++;
    }
  }

  private boolean executeHealthCheck() {
    SpansResponse response = GrpcClientRequestContextUtil.executeInTenantContext(defaultTenant,
            () -> client.withDeadline(Deadline.after(timeout, TimeUnit.SECONDS))
                    .getSpans(buildSpanRequest()));
    return response.getSpansCount() >= 0;
  }

  private SpansRequest buildSpanRequest() {
    return SpansRequest.newBuilder()
            .setStartTimeMillis(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10))
            .setEndTimeMillis(System.currentTimeMillis())
            .addSelection(QueryExpressionUtil.getColumnExpression("EVENT.id"))
            .setLimit(1)
            .build();
  }

  private boolean executePinotHealthCheck() throws Exception {
    HttpURLConnection con = null;
    try {
      URL url = new URL(String.format("http://%s:%s/health", pinotSeverHost, pinotServerPort));
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(timeout * 1000);
      con.setReadTimeout(timeout * 1000);
      con.connect();
      int status = con.getResponseCode();

      if (status >= 200 && status <= 206) {
          return true;
      }
      return false;
    } finally {
      if (con != null) { con.disconnect(); }
    }
  }

  private boolean executeBrokerRegistrationCheck() throws Exception {
    HttpURLConnection con = null;
    try {
      URL url = new URL(String.format("http://%s:%s/brokers/tenants", pinotSeverHost, pinotControllerPort));
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(timeout * 1000);
      con.setReadTimeout(timeout * 1000);
      con.connect();
      int status = con.getResponseCode();

      if (status >= 200 && status <= 206) {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          content.append(inputLine);
        }
        in.close();
        String trimmed = content.toString().trim();
        if (trimmed != null && trimmed.contains("DefaultTenant")) {
          return true;
        }
      }
      return false;
    } finally {
      if (con != null) { con.disconnect(); }
    }
  }
}
