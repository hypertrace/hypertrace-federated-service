package org.hypertrace.graphql.service;

import com.typesafe.config.Config;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.hypertrace.core.graphql.spi.config.GraphQlServiceConfig;
import org.hypertrace.graphql.config.HypertraceGraphQlServiceConfig;
import org.hypertrace.graphql.impl.GraphQlFactory;

public class GraphQlServiceImpl {
  private HypertraceGraphQlServiceConfig graphQlServiceConfig;
  private DefaultGraphQlServiceLifecycle serviceLifecycle;
  private ServletContextHandler contextHandler;

  public GraphQlServiceImpl(Config appConfig) {
    this.graphQlServiceConfig = new DefaultGraphQlServiceConfig(appConfig);
    this.serviceLifecycle = new DefaultGraphQlServiceLifecycle();

    contextHandler = new ServletContextHandler();
    if (this.graphQlServiceConfig.isCorsEnabled()) {
      contextHandler.addFilter(
              CrossOriginFilter.class,
              this.graphQlServiceConfig.getGraphqlUrlPath(),
              EnumSet.of(DispatcherType.REQUEST));
    }

    contextHandler.addServlet(
            new ServletHolder(
                    new GraphQlServiceHttpServlet(
                            GraphQlFactory.build(this.graphQlServiceConfig, this.serviceLifecycle))),
            this.graphQlServiceConfig.getGraphqlUrlPath());

  }

  public GraphQlServiceConfig getGraphQlServiceConfig() {
    return graphQlServiceConfig;
  }

  public ServletContextHandler getContextHandler() {
    return contextHandler;
  }

  public void shutdown() {
    if (serviceLifecycle != null) {
      serviceLifecycle.shutdown();
    }
  }
}
