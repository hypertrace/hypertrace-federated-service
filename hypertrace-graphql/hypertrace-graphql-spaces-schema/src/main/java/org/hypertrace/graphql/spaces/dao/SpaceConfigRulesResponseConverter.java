package org.hypertrace.graphql.spaces.dao;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import javax.inject.Inject;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hypertrace.core.graphql.common.utils.attributes.AttributeScopeStringTranslator;
import org.hypertrace.graphql.spaces.schema.query.SpaceConfigRuleResultSet;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleAttributeValueRule;
import org.hypertrace.graphql.spaces.schema.shared.SpaceConfigRuleType;
import org.hypertrace.spaces.config.service.v1.AttributeValueRuleData;
import org.hypertrace.spaces.config.service.v1.CreateRuleResponse;
import org.hypertrace.spaces.config.service.v1.GetRulesResponse;
import org.hypertrace.spaces.config.service.v1.UpdateRuleResponse;

@Slf4j
class SpaceConfigRulesResponseConverter {

  private final AttributeScopeStringTranslator scopeStringTranslator;

  @Inject
  SpaceConfigRulesResponseConverter(AttributeScopeStringTranslator scopeStringTranslator) {
    this.scopeStringTranslator = scopeStringTranslator;
  }

  Single<SpaceConfigRuleResultSet> convertGetResponse(GetRulesResponse rulesResponse) {
    return Observable.fromIterable(rulesResponse.getRulesList())
        .flatMapMaybe(this::convertRule)
        .toList()
        .map(ConvertedSpaceConfigRuleResultSet::forRuleList);
  }

  Single<SpaceConfigRule> convertRule(CreateRuleResponse createRuleResponse) {
    return this.convertRule(createRuleResponse.getRule())
        .switchIfEmpty(
            Single.error(new IllegalArgumentException("Unable to convert rule creation response")));
  }

  Single<Boolean> buildDeleteResponse() {
    return Single.just(true);
  }

  Single<SpaceConfigRule> convertRule(UpdateRuleResponse updateRuleResponse) {
    return this.convertRule(updateRuleResponse.getRule())
        .switchIfEmpty(
            Single.error(new IllegalArgumentException("Unable to convert rule update response")));
  }

  private Maybe<SpaceConfigRule> convertRule(
      org.hypertrace.spaces.config.service.v1.SpaceConfigRule rule) {
    switch (rule.getRuleDataCase()) {
      case ATTRIBUTE_VALUE_RULE_DATA:
        return Maybe.just(
            ConvertedSpaceConfigRule.forAttributeValueRule(
                rule.getId(), this.convertAttributeValueRule(rule.getAttributeValueRuleData())));
      case RULEDATA_NOT_SET:
      default:
        log.error("Unrecognized rule type {}", rule.getRuleDataCase().name());
        return Maybe.empty();
    }
  }

  private SpaceConfigRuleAttributeValueRule convertAttributeValueRule(
      AttributeValueRuleData ruleData) {
    return new ConvertedSpaceConfigRuleAttributeValueRule(
        this.scopeStringTranslator.toExternal(ruleData.getAttributeScope()),
        ruleData.getAttributeKey());
  }

  @Value
  @Accessors(fluent = true)
  private static class ConvertedSpaceConfigRuleAttributeValueRule
      implements SpaceConfigRuleAttributeValueRule {
    String attributeScope;
    String attributeKey;
  }

  @Value
  @Accessors(fluent = true)
  private static class ConvertedSpaceConfigRule implements SpaceConfigRule {
    private static SpaceConfigRule forAttributeValueRule(
        String id, SpaceConfigRuleAttributeValueRule rule) {
      return new ConvertedSpaceConfigRule(id, SpaceConfigRuleType.ATTRIBUTE_VALUE, rule);
    }

    String id;
    SpaceConfigRuleType type;
    SpaceConfigRuleAttributeValueRule attributeValueRule;
  }

  @Value
  @Accessors(fluent = true)
  private static class ConvertedSpaceConfigRuleResultSet implements SpaceConfigRuleResultSet {
    private static SpaceConfigRuleResultSet forRuleList(List<SpaceConfigRule> ruleList) {
      return new ConvertedSpaceConfigRuleResultSet(ruleList, ruleList.size(), ruleList.size());
    }

    List<SpaceConfigRule> results;
    long total;
    long count;
  }
}
