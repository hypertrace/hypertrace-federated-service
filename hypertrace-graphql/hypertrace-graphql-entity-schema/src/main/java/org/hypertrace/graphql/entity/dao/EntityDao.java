package org.hypertrace.graphql.entity.dao;

import io.reactivex.rxjava3.core.Single;
import org.hypertrace.graphql.entity.request.EntityRequest;
import org.hypertrace.graphql.entity.schema.EntityResultSet;

public interface EntityDao {

  Single<EntityResultSet> getEntities(EntityRequest request);
}
