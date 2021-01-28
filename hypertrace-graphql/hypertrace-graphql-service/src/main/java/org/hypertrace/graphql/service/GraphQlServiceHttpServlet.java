package org.hypertrace.graphql.service;

import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;

class GraphQlServiceHttpServlet extends GraphQLHttpServlet {

  private final GraphQLConfiguration configuration;

  GraphQlServiceHttpServlet(GraphQLConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected GraphQLConfiguration getConfiguration() {
    return configuration;
  }
}
