package org.hypertrace.core.query.service.pinot;

import org.apache.pinot.client.ResultSet;

public class DefaultResultSetTypePredicateProvider implements ResultSetTypePredicateProvider {
  @Override
  public boolean isSelectionResultSetType(ResultSet resultSet) {
    return resultSet.getClass().getName().contains("SelectionResultSet");
  }

  @Override
  public boolean isResultTableResultSetType(ResultSet resultSet) {
    return resultSet.getClass().getName().contains("ResultTableResultSet");
  }
}
