package org.hypertrace.graphql.explorer.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.graphql.explorer.request.ExploreRequest;
import org.hypertrace.graphql.explorer.schema.ExploreResultSet;

public interface ExplorerDao {
  Single<ExploreResultSet> explore(ExploreRequest request);
}
