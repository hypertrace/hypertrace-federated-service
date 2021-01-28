package org.hypertrace.graphql.spaces.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;
import org.hypertrace.graphql.spaces.schema.query.SpaceResultSet;

public interface SpacesDao {
  Single<SpaceResultSet> getAllSpaces(GraphQlRequestContext context);
}
