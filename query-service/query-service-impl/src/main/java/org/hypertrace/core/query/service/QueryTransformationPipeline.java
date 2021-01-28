package org.hypertrace.core.query.service;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Set;
import javax.inject.Inject;
import org.hypertrace.core.query.service.QueryTransformation.QueryTransformationContext;
import org.hypertrace.core.query.service.api.QueryRequest;

/**
 * A query transformation pipeline that invokes each registered transformation and applies the
 * result to the next transformation. In the current implementation, there is no guarantee of the
 * transformation order to allow decoupled registration, but this may change in the future.
 */
class QueryTransformationPipeline {
  private final Set<QueryTransformation> transformations;

  @Inject
  QueryTransformationPipeline(Set<QueryTransformation> transformations) {
    this.transformations = transformations;
  }

  Single<QueryRequest> transform(QueryRequest originalRequest, String tenantId) {
    QueryTransformationContext transformationContext =
        new DefaultQueryTransformationContext(tenantId);
    return Observable.fromIterable(transformations)
        .reduce(
            Single.just(originalRequest),
            (requestSingle, transformation) ->
                requestSingle.flatMap(
                    request -> transformation.transform(request, transformationContext)))
        .flatMap(request -> request);
  }

  private static class DefaultQueryTransformationContext implements QueryTransformationContext {
    private final String tenantId;

    private DefaultQueryTransformationContext(String tenantId) {
      this.tenantId = tenantId;
    }

    public String getTenantId() {
      return tenantId;
    }
  }
}
