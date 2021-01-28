package org.hypertrace.core.bootstrapper.commands;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import java.util.List;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.v1.AttributeCreateRequest;
import org.hypertrace.core.attribute.service.v1.AttributeMetadata;
import org.hypertrace.core.attribute.service.v1.AttributeMetadataFilter;
import org.hypertrace.core.attribute.service.v1.AttributeScope;
import org.hypertrace.core.bootstrapper.BootstrapContext;

/** Command class for attribute metadata related operations */
@CommandType(type = "Attribute")
public class AttributeCommand extends Command {
  private static final Parser PARSER = JsonFormat.parser().ignoringUnknownFields();

  public AttributeCommand(BootstrapContext bootstrapContext) {
    super(bootstrapContext);
  }

  public class ADD extends Operation {
    private static final String DATA_PATH = "attributes";

    public ADD(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      bootstrapContext
          .getAttributesServiceClient()
          .create(getTenantId(), getAttributeCreateRequest());
    }

    private AttributeCreateRequest getAttributeCreateRequest() {
      // Create all
      List<? extends Config> attrMetadataConfigs = config.getConfigList(DATA_PATH);
      AttributeCreateRequest.Builder createRequestBuilder = AttributeCreateRequest.newBuilder();
      for (Config attrMetadataConfig : attrMetadataConfigs) {
        try {
          AttributeMetadata.Builder attributeMetadataBuilder = AttributeMetadata.newBuilder();
          String configString =
              attrMetadataConfig.root().render(ConfigRenderOptions.concise().setJson(true));
          PARSER.merge(configString, attributeMetadataBuilder);
          createRequestBuilder.addAttributes(attributeMetadataBuilder);
        } catch (InvalidProtocolBufferException ex) {
          throw new RuntimeException(
              String.format(
                  "Failed to construct AttributeMetadata from config: %s", attrMetadataConfig));
        }
      }
      return createRequestBuilder.build();
    }
  }

  public class DELETE extends Operation {

    public DELETE(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      bootstrapContext
          .getAttributesServiceClient()
          .delete(getTenantId(), getAttributeMetadataFilter());
    }

    private AttributeMetadataFilter getAttributeMetadataFilter() {
      AttributeMetadataFilter.Builder builder = AttributeMetadataFilter.newBuilder();
      Config filterConfig = config.getConfig("filter");
      if (filterConfig.hasPath("key")) {
        builder.addAllKey(filterConfig.getStringList("key"));
      }
      if (filterConfig.hasPath("scope")) {
        builder.addAllScope(
            filterConfig.getStringList("scope").stream()
                .map(AttributeScope::valueOf)
                .collect(Collectors.toList()));
      }
      return builder.build();
    }
  }
}
