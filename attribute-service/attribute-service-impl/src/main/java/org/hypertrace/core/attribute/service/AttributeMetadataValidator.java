package org.hypertrace.core.attribute.service;

import static org.hypertrace.core.attribute.service.util.AttributeScopeUtil.resolveScopeString;

import com.google.common.base.Strings;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.v1.AttributeCreateRequest;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.attribute.service.v1.AttributeType;

/** Validates {@link AttributeCreateRequest} */
public class AttributeMetadataValidator {

  public static void validate(AttributeCreateRequest attributeCreateRequest) {
    attributeCreateRequest.getAttributesList().forEach(AttributeMetadataValidator::validate);

    // Ensure Scope + Key is unique
    List<Map.Entry<String, String>> duplicateScopeKeys =
        attributeCreateRequest.getAttributesList().stream()
            .collect(
                Collectors.groupingBy(
                    attributeMetadata ->
                        new AbstractMap.SimpleEntry<>(
                            resolveScopeString(attributeMetadata), attributeMetadata.getKey())))
            .entrySet()
            .stream()
            .filter(attributeMetadataList -> attributeMetadataList.getValue().size() > 1)
            .map(Entry::getKey)
            .collect(Collectors.toList());
    if (!duplicateScopeKeys.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Duplicate scope + key found for:%s", duplicateScopeKeys));
    }

    // Ensure Scope + FQN is unique
    List<Map.Entry<String, String>> duplicateScopeFQNs =
        attributeCreateRequest.getAttributesList().stream()
            .collect(
                Collectors.groupingBy(
                    attributeMetadata ->
                        new AbstractMap.SimpleEntry<>(
                            resolveScopeString(attributeMetadata), attributeMetadata.getFqn())))
            .entrySet()
            .stream()
            .filter(attributeMetadataList -> attributeMetadataList.getValue().size() > 1)
            .map(Entry::getKey)
            .collect(Collectors.toList());
    if (!duplicateScopeFQNs.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Duplicate scope + FQN found for:%s", duplicateScopeFQNs));
    }
  }

  private static void validate(AttributeMetadata attributeMetadata) {
    if (resolveScopeString(attributeMetadata).equals(AttributeScope.SCOPE_UNDEFINED.name())
        || Strings.isNullOrEmpty(attributeMetadata.getKey())
        || Strings.isNullOrEmpty(attributeMetadata.getFqn())
        || attributeMetadata.getValueKind().equals(AttributeKind.KIND_UNDEFINED)
        || attributeMetadata.getValueKind().equals(AttributeKind.UNRECOGNIZED)
        || attributeMetadata.getType().equals(AttributeType.UNRECOGNIZED)
        || attributeMetadata.getType().equals(AttributeType.TYPE_UNDEFINED)) {
      throw new IllegalArgumentException(String.format("Invalid attribute:%s", attributeMetadata));
    }
  }
}
