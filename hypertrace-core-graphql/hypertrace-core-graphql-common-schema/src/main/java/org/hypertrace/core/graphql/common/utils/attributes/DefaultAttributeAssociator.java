package org.hypertrace.core.graphql.common.utils.attributes;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.Collection;
import java.util.function.Function;
import javax.inject.Inject;
import org.hypertrace.core.graphql.attributes.AttributeModelScope;
import org.hypertrace.core.graphql.attributes.AttributeStore;
import org.hypertrace.core.graphql.common.request.AttributeAssociation;
import org.hypertrace.core.graphql.context.GraphQlRequestContext;

class DefaultAttributeAssociator implements AttributeAssociator {

  private final AttributeStore attributeStore;

  @Inject
  DefaultAttributeAssociator(AttributeStore attributeStore) {
    this.attributeStore = attributeStore;
  }

  @Override
  public <T> Observable<AttributeAssociation<T>> associateAttributes(
      GraphQlRequestContext context,
      AttributeModelScope requestScope,
      Collection<T> inputs,
      Function<T, String> attributeKeyMapper) {
    return Observable.fromIterable(inputs)
        .flatMapSingle(
            input -> this.associateAttribute(context, requestScope, input, attributeKeyMapper));
  }

  @Override
  public <T> Single<AttributeAssociation<T>> associateAttribute(
      GraphQlRequestContext context,
      AttributeModelScope requestScope,
      T input,
      Function<T, String> attributeKeyMapper) {
    return this.attributeStore
        .get(context, requestScope, attributeKeyMapper.apply(input))
        .map(attribute -> AttributeAssociation.of(attribute, input));
  }
}
