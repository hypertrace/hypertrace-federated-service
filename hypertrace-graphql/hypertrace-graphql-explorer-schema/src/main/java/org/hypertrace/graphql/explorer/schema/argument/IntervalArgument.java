package org.hypertrace.graphql.explorer.schema.argument;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import org.hypertrace.core.graphql.common.schema.time.TimeUnit;

@GraphQLName(IntervalArgument.TYPE_NAME)
public interface IntervalArgument {
  String TYPE_NAME = "IntervalArgument";
  String ARGUMENT_NAME = "interval";
  String INTERVAL_SIZE_KEY = "size";
  String INTERVAL_UNITS_KEY = "units";

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(INTERVAL_SIZE_KEY)
  int size();

  @GraphQLField
  @GraphQLNonNull
  @GraphQLName(INTERVAL_UNITS_KEY)
  TimeUnit units();
}
