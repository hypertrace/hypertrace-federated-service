package org.hypertrace.core.query.service;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerClientConfig;
import org.hypertrace.core.query.service.QueryServiceConfig.RequestHandlerConfig;
import org.hypertrace.core.query.service.pinot.ViewDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueryServiceConfigTest {
  private Config appConfig;
  private QueryServiceConfig queryServiceConfig;

  @BeforeEach
  public void setup() {
    appConfig =
        ConfigFactory.parseURL(
            requireNonNull(
                QueryServiceConfigTest.class
                    .getClassLoader()
                    .getResource("application.conf")));
    queryServiceConfig = new QueryServiceConfig(appConfig.getConfig("service.config"));
  }

  @Test
  public void testQueryServiceImplConfigParser() {
    // Test QueryServiceImplConfig
    assertEquals("query-service", appConfig.getString("service.name"));
    assertEquals(8091, appConfig.getInt("service.admin.port"));
    assertEquals(8090, appConfig.getInt("service.port"));
    assertEquals(4, queryServiceConfig.getQueryRequestHandlersConfigs().size());
    assertEquals(2, queryServiceConfig.getRequestHandlerClientConfigs().size());

    RequestHandlerConfig handler0 = queryServiceConfig.getQueryRequestHandlersConfigs().get(0);
    assertEquals("trace-view-handler", handler0.getName());
    assertEquals("pinot", handler0.getType());
    Config requestHandlerInfo = handler0.getRequestHandlerInfo();

    String tenantColumnName = "tenant_id";
    ViewDefinition viewDefinition =
        ViewDefinition.parse(requestHandlerInfo.getConfig("viewDefinition"), tenantColumnName);
    assertEquals("RawTraceView", viewDefinition.getViewName());
    assertEquals(tenantColumnName, viewDefinition.getTenantIdColumn());

    Map<String, RequestHandlerClientConfig> clientConfigMap =
        queryServiceConfig.getRequestHandlerClientConfigs().stream()
            .collect(Collectors.toMap(RequestHandlerClientConfig::getType, Function.identity()));
    RequestHandlerClientConfig clientConfig0 = clientConfigMap.get(handler0.getClientConfig());
    assertEquals("broker", clientConfig0.getType());
    assertEquals("pinotCluster0:8099", clientConfig0.getConnectionString());

    RequestHandlerConfig handler1 = queryServiceConfig.getQueryRequestHandlersConfigs().get(1);
    assertEquals("span-event-view-handler", handler1.getName());
    assertEquals("pinot", handler1.getType());
    RequestHandlerClientConfig clientConfig1 = clientConfigMap.get(handler1.getClientConfig());
    assertEquals("zookeeper", clientConfig1.getType());
    assertEquals("pinotCluster1:2181", clientConfig1.getConnectionString());

    assertEquals("localhost", queryServiceConfig.getAttributeClientConfig().getHost());
    assertEquals(9012, queryServiceConfig.getAttributeClientConfig().getPort());
  }
}
