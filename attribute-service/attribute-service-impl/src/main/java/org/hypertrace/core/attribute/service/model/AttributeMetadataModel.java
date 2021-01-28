package org.hypertrace.core.attribute.service.model;

import static org.hypertrace.core.attribute.service.util.AttributeScopeUtil.resolveScope;
import static org.hypertrace.core.attribute.service.util.AttributeScopeUtil.resolveScopeString;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.v1.AggregateFunction;
import org.hypertrace.core.attribute.service.v1.AttributeDefinition;
import org.hypertrace.core.attribute.service.v1.AttributeKind;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.attribute.service.v1.AttributeSource;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeType;
import org.hypertrace.core.documentstore.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class used to convert the Proto message to/from its JSON representation in the DocStore */
public class AttributeMetadataModel implements Document {

  private static final Logger LOGGER = LoggerFactory.getLogger(AttributeMetadataModel.class);
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private String fqn;

  @JsonProperty(value = "value_kind")
  private AttributeKind valueKind;

  private String key;

  @JsonProperty(value = "display_name")
  private String displayName;

  @JsonProperty(value = "scope_string")
  private String scopeString;

  private boolean materialized;
  private String unit;
  private AttributeType type;
  private List<String> labels = Collections.emptyList();

  @JsonProperty(value = "tenant_id")
  private String tenantId;

  private Boolean groupable;

  private List<AggregateFunction> supportedAggregations = Collections.emptyList();
  private boolean onlyAggregationsAllowed;
  private List<AttributeSource> sources = Collections.emptyList();
  private Map<String, Map<String, String>> metadata = Collections.emptyMap();

  @JsonSerialize(using = ProtobufMessageSerializer.class)
  @JsonDeserialize(using = AttributeDefinitionDeserializer.class)
  private AttributeDefinition definition = AttributeDefinition.getDefaultInstance();

  protected AttributeMetadataModel() {}

  public static AttributeMetadataModel fromDTO(AttributeMetadata attributeMetadata) {
    AttributeMetadataModel attributeMetadataModel = new AttributeMetadataModel();
    attributeMetadataModel.setFqn(attributeMetadata.getFqn());
    attributeMetadataModel.setValueKind(attributeMetadata.getValueKind());
    attributeMetadataModel.setKey(attributeMetadata.getKey());
    attributeMetadataModel.setUnit(attributeMetadata.getUnit());
    attributeMetadataModel.setType(attributeMetadata.getType());
    attributeMetadataModel.setScopeString(resolveScopeString(attributeMetadata));
    attributeMetadataModel.setMaterialized(attributeMetadata.getMaterialized());
    attributeMetadataModel.setDisplayName(attributeMetadata.getDisplayName());
    attributeMetadataModel.setLabels(attributeMetadata.getLabelsList());
    attributeMetadataModel.setSupportedAggregations(
        attributeMetadata.getSupportedAggregationsList());
    attributeMetadataModel.setOnlyAggregationsAllowed(
        attributeMetadata.getOnlyAggregationsAllowed());
    attributeMetadataModel.setSources(attributeMetadata.getSourcesList());
    attributeMetadataModel.setGroupable(attributeMetadata.getGroupable());
    attributeMetadataModel.setDefinition(attributeMetadata.getDefinition());
    attributeMetadataModel.setMetadata(
        attributeMetadata.getMetadataMap().entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    stringAttributeSourceMetadataEntry ->
                        stringAttributeSourceMetadataEntry.getValue().getSourceMetadataMap())));
    return attributeMetadataModel;
  }

  public static AttributeMetadataModel fromJson(String json) throws IOException {
    return OBJECT_MAPPER.readValue(json, AttributeMetadataModel.class);
  }

  public String getId() {
    return this.getScopeString() + "." + key;
  }

  public String getFqn() {
    return fqn;
  }

  public void setFqn(String fqn) {
    this.fqn = fqn;
  }

  public AttributeKind getValueKind() {
    return valueKind;
  }

  public void setValueKind(AttributeKind valueKind) {
    this.valueKind = valueKind;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getScopeString() {
    return this.scopeString;
  }

  public void setScopeString(String scopeString) {
    this.scopeString = scopeString;
  }

  @JsonProperty // Retain for backwards compat with existing JSON docs
  private void setScope(AttributeScope scope) {
    this.scopeString = resolveScopeString(scope);
  }

  public boolean isMaterialized() {
    return materialized;
  }

  public void setMaterialized(boolean materialized) {
    this.materialized = materialized;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public AttributeType getType() {
    return type;
  }

  public void setType(AttributeType type) {
    this.type = type;
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public boolean isGroupable() {
    return groupable != null
        ? groupable.booleanValue()
        : AttributeKind.TYPE_STRING.equals(valueKind);
  }

  public void setGroupable(boolean groupable) {
    this.groupable = groupable;
  }

  public List<AttributeSource> getSources() {
    return sources;
  }

  public void setSources(List<AttributeSource> sources) {
    this.sources = sources;
  }

  public List<AggregateFunction> getSupportedAggregations() {
    return supportedAggregations;
  }

  public void setSupportedAggregations(List<AggregateFunction> supportedAggregations) {
    this.supportedAggregations = supportedAggregations;
  }

  public boolean isOnlyAggregationsAllowed() {
    return onlyAggregationsAllowed;
  }

  public void setOnlyAggregationsAllowed(boolean onlyAggregationsAllowed) {
    this.onlyAggregationsAllowed = onlyAggregationsAllowed;
  }

  public void setMetadata(Map<String, Map<String, String>> metadata) {
    this.metadata = metadata;
  }

  public AttributeDefinition getDefinition() {
    return definition;
  }

  public void setDefinition(AttributeDefinition definition) {
    this.definition = definition;
  }

  public AttributeMetadata toDTO() {
    return toDTOBuilder().build();
  }

  public AttributeMetadata.Builder toDTOBuilder() {
    AttributeMetadata.Builder builder =
        AttributeMetadata.newBuilder()
            .setFqn(fqn)
            .setId(getId())
            .setKey(key)
            .setScope(resolveScope(scopeString))
            .setScopeString(getScopeString())
            .setValueKind(valueKind)
            .setDisplayName(displayName)
            .setMaterialized(materialized)
            .setGroupable(isGroupable())
            .setType(type)
            .addAllLabels(labels)
            .addAllSupportedAggregations(supportedAggregations)
            .setOnlyAggregationsAllowed(onlyAggregationsAllowed)
            .addAllSources(sources)
            .putAllMetadata(
                metadata.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            Map.Entry::getKey,
                            stringMapEntry ->
                                AttributeSourceMetadata.newBuilder()
                                    .putAllSourceMetadata(stringMapEntry.getValue())
                                    .build())))
            .setDefinition(this.definition);

    if (unit != null) {
      builder.setUnit(unit);
    }

    return builder;
  }

  @Override
  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      LOGGER.error("Error in converting {} to json", this);
      throw new RuntimeException("Error in converting AttributeMetadataModel to json");
    }
  }

  @Override
  public String toString() {
    return "AttributeMetadataModel{"
        + "fqn='"
        + fqn
        + '\''
        + ", valueKind="
        + valueKind
        + ", key='"
        + key
        + '\''
        + ", displayName='"
        + displayName
        + '\''
        + ", scopeString='"
        + scopeString
        + '\''
        + ", materialized="
        + materialized
        + ", unit='"
        + unit
        + '\''
        + ", type="
        + type
        + ", labels="
        + labels
        + ", tenantId='"
        + tenantId
        + '\''
        + ", groupable="
        + groupable
        + ", supportedAggregations="
        + supportedAggregations
        + ", onlyAggregationsAllowed="
        + onlyAggregationsAllowed
        + ", sources="
        + sources
        + ", metadata="
        + metadata
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttributeMetadataModel that = (AttributeMetadataModel) o;
    return materialized == that.materialized
        && onlyAggregationsAllowed == that.onlyAggregationsAllowed
        && Objects.equals(fqn, that.fqn)
        && valueKind == that.valueKind
        && Objects.equals(key, that.key)
        && Objects.equals(displayName, that.displayName)
        && Objects.equals(unit, that.unit)
        && type == that.type
        && Objects.equals(labels, that.labels)
        && Objects.equals(tenantId, that.tenantId)
        && Objects.equals(groupable, that.groupable)
        && Objects.equals(supportedAggregations, that.supportedAggregations)
        && Objects.equals(sources, that.sources)
        && Objects.equals(metadata, that.metadata)
        && Objects.equals(definition, that.definition)
        && Objects.equals(scopeString, that.scopeString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        fqn,
        valueKind,
        key,
        displayName,
        materialized,
        unit,
        type,
        labels,
        tenantId,
        groupable,
        supportedAggregations,
        onlyAggregationsAllowed,
        sources,
        metadata,
        definition,
        scopeString);
  }

  private static class ProtobufMessageSerializer extends JsonSerializer<Message> {
    private static final JsonFormat.Printer PRINTER =
        JsonFormat.printer().omittingInsignificantWhitespace();

    @Override
    public void serialize(Message message, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
      generator.writeRawValue(PRINTER.print(message));
    }
  }

  private static class AttributeDefinitionDeserializer extends JsonDeserializer<Message> {
    private static final JsonFormat.Parser PARSER = JsonFormat.parser();

    @Override
    public Message deserialize(JsonParser parser, DeserializationContext context)
        throws IOException {
      AttributeDefinition.Builder builder = AttributeDefinition.newBuilder();
      PARSER.merge(parser.readValueAsTree().toString(), builder);
      return builder.build();
    }
  }
}
