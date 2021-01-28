package org.hypertrace.graphql.explorer.fetcher;

import java.util.Map;
import org.hypertrace.graphql.explorer.schema.ExploreResult;
import org.hypertrace.graphql.explorer.schema.Selection;

public interface MapBackedExploreResult extends ExploreResult {
  Map<ExploreResultMapKey, Selection> selectionMap();
}
