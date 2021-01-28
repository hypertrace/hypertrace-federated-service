package org.hypertrace.core.query.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.hypertrace.core.query.service.api.ColumnIdentifier;
import org.hypertrace.core.query.service.api.ColumnMetadata;
import org.hypertrace.core.query.service.api.Expression;
import org.hypertrace.core.query.service.api.Expression.ValueCase;
import org.hypertrace.core.query.service.api.Filter;
import org.hypertrace.core.query.service.api.Function;
import org.hypertrace.core.query.service.api.OrderByExpression;
import org.hypertrace.core.query.service.api.QueryRequest;
import org.hypertrace.core.query.service.api.ResultSetMetadata;
import org.hypertrace.core.query.service.api.ValueType;

/**
 * Wrapper class to hold the query execution context that is needed by different components
 * during the life cycles of a request.
 */
public class ExecutionContext {

  private final String tenantId;
  private Set<String> referencedColumns;
  private final LinkedHashSet<String> selectedColumns;
  private ResultSetMetadata resultSetMetadata;
  // Contains all selections to be made in the DB: selections on group by, single columns and
  // aggregations in that order.
  // There should be a one-to-one mapping between this and the columnMetadataSet in
  // ResultSetMetadata.
  // The difference between this and selectedColumns above is that this is a set of Expressions
  // while the selectedColumns
  // is a set of column names.
  private final LinkedHashSet<Expression> allSelections;

  public ExecutionContext(String tenantId, QueryRequest request) {
    this.tenantId = tenantId;
    this.selectedColumns = new LinkedHashSet<>();
    this.allSelections = new LinkedHashSet<>();
    analyze(request);
  }

  private void analyze(QueryRequest request) {
    List<String> filterColumns = new ArrayList<>();
    LinkedList<Filter> filterQueue = new LinkedList<>();
    filterQueue.add(request.getFilter());
    while (!filterQueue.isEmpty()) {
      Filter filter = filterQueue.pop();
      if (filter.getChildFilterCount() > 0) {
        filterQueue.addAll(filter.getChildFilterList());
      } else {
        extractColumns(filterColumns, filter.getLhs());
        extractColumns(filterColumns, filter.getRhs());
      }
    }

    List<String> postFilterColumns = new ArrayList<>();
    List<String> selectedList = new ArrayList<>();
    LinkedHashSet<ColumnMetadata> columnMetadataSet = new LinkedHashSet<>();

    // group by columns must be first in the response
    if (request.getGroupByCount() > 0) {
      for (Expression expression : request.getGroupByList()) {
        extractColumns(postFilterColumns, expression);
        columnMetadataSet.add(toColumnMetadata(expression));
        allSelections.add(expression);
      }
    }
    if (request.getSelectionCount() > 0) {
      for (Expression expression : request.getSelectionList()) {
        extractColumns(selectedList, expression);
        postFilterColumns.addAll(selectedList);
        columnMetadataSet.add(toColumnMetadata(expression));
        allSelections.add(expression);
      }
    }
    if (request.getAggregationCount() > 0) {
      for (Expression expression : request.getAggregationList()) {
        extractColumns(postFilterColumns, expression);
        columnMetadataSet.add(toColumnMetadata(expression));
        allSelections.add(expression);
      }
    }

    referencedColumns = new HashSet<>();
    referencedColumns.addAll(filterColumns);
    referencedColumns.addAll(postFilterColumns);
    resultSetMetadata =
        ResultSetMetadata.newBuilder().addAllColumnMetadata(columnMetadataSet).build();
    selectedColumns.addAll(selectedList);
  }

  private ColumnMetadata toColumnMetadata(Expression expression) {
    ColumnMetadata.Builder builder = ColumnMetadata.newBuilder();
    ValueCase valueCase = expression.getValueCase();
    switch (valueCase) {
      case COLUMNIDENTIFIER:
        ColumnIdentifier columnIdentifier = expression.getColumnIdentifier();
        String alias = columnIdentifier.getAlias();
        if (alias != null && alias.trim().length() > 0) {
          builder.setColumnName(alias);
        } else {
          builder.setColumnName(columnIdentifier.getColumnName());
        }
        builder.setValueType(ValueType.STRING);
        builder.setIsRepeated(false);
        break;
      case FUNCTION:
        Function function = expression.getFunction();
        alias = function.getAlias();
        if (alias != null && alias.trim().length() > 0) {
          builder.setColumnName(alias);
        } else {
          // todo: handle recursive functions max(rollup(time,50)
          // workaround is to use alias for now
          builder.setColumnName(function.getFunctionName());
        }
        builder.setValueType(ValueType.STRING);
        builder.setIsRepeated(false);
        break;
      case LITERAL:
      case ORDERBY:
      case VALUE_NOT_SET:
        break;
    }
    return builder.build();
  }

  private void extractColumns(List<String> columns, Expression expression) {
    ValueCase valueCase = expression.getValueCase();
    switch (valueCase) {
      case COLUMNIDENTIFIER:
        ColumnIdentifier columnIdentifier = expression.getColumnIdentifier();
        columns.add(columnIdentifier.getColumnName());
        break;
      case LITERAL:
        // no columns
        break;
      case FUNCTION:
        Function function = expression.getFunction();
        for (Expression childExpression : function.getArgumentsList()) {
          extractColumns(columns, childExpression);
        }
        break;
      case ORDERBY:
        OrderByExpression orderBy = expression.getOrderBy();
        extractColumns(columns, orderBy.getExpression());
        break;
      case VALUE_NOT_SET:
        break;
    }
  }

  public String getTenantId() {
    return this.tenantId;
  }

  public Set<String> getReferencedColumns() {
    return referencedColumns;
  }

  public ResultSetMetadata getResultSetMetadata() {
    return resultSetMetadata;
  }

  public LinkedHashSet<String> getSelectedColumns() {
    return selectedColumns;
  }

  public LinkedHashSet<Expression> getAllSelections() {
    return this.allSelections;
  }
}
