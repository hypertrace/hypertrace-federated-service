package org.hypertrace.core.query.service;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.core.query.service.api.QueryRequest;

public interface QueryTransformation {
  Single<QueryRequest> transform(QueryRequest queryRequest, QueryTransformationContext transformationContext);

  interface QueryTransformationContext {}
}
