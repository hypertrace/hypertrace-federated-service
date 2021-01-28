package org.hypertrace.core.query.service;


/**
 * Canonical query function names received in requests. These should be converted to data-store
 * specific names when handling a request.
 */
public interface QueryFunctionConstants {
  String QUERY_FUNCTION_SUM = "SUM";
  String QUERY_FUNCTION_AVG = "AVG";
  String QUERY_FUNCTION_MIN = "MIN";
  String QUERY_FUNCTION_MAX = "MAX";
  String QUERY_FUNCTION_COUNT = "COUNT";
  String QUERY_FUNCTION_PERCENTILE = "PERCENTILE";
  String QUERY_FUNCTION_DISTINCTCOUNT = "DISTINCTCOUNT";
  String QUERY_FUNCTION_CONCAT = "CONCAT";
  String QUERY_FUNCTION_HASH = "HASH";
  String QUERY_FUNCTION_STRINGEQUALS = "STRINGEQUALS";
  String QUERY_FUNCTION_CONDITIONAL = "CONDITIONAL";
}
