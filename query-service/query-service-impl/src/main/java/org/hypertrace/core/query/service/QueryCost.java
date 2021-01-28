package org.hypertrace.core.query.service;

public class QueryCost {

  public static QueryCost UNSUPPORTED = new QueryCost(-1);

  private final double cost;

  public QueryCost(double cost) {
    this.cost = cost;
  }

  /**
   * Return the cost to evaluate the request.
   *
   * @return -1 means it cannot handle the request else 0 (super fast) to 1 very expensive
   */
  public double getCost() {
    return cost;
  }
}
