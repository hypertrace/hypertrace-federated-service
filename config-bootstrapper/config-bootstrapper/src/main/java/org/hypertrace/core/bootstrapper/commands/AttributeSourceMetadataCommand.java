package org.hypertrace.core.bootstrapper.commands;

import com.typesafe.config.Config;
import java.util.Map;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.v1.AttributeSource;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadataDeleteRequest;
import org.hypertrace.core.attribute.service.v1.AttributeSourceMetadataUpdateRequest;
import org.hypertrace.core.bootstrapper.BootstrapContext;

/** Command class for Attribute Source metadata related operations */
@CommandType(type = "AttributeSourceMetadata")
public class AttributeSourceMetadataCommand extends Command {

  public AttributeSourceMetadataCommand(BootstrapContext bootstrapContext) {
    super(bootstrapContext);
  }

  public class ADD extends Operation {
    private static final String SOURCE = "source";
    private static final String METADATA = "metadata";

    public ADD(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      bootstrapContext
          .getAttributesServiceClient()
          .updateSourceMetadata(getTenantId(), getAttributeSourceMetadataUpdateRequest(config));
    }

    private AttributeSourceMetadataUpdateRequest getAttributeSourceMetadataUpdateRequest(
        Config config) {
      return AttributeSourceMetadataUpdateRequest.newBuilder()
          .setSource(AttributeSource.valueOf(config.getString(SOURCE)))
          .putAllSourceMetadata(
              config.getObject(METADATA).unwrapped().entrySet().stream()
                  .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().toString())))
          .build();
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
          .deleteSourceMetadata(getTenantId(), getAttributeSourceMetadataDeleteRequest());
    }

    private AttributeSourceMetadataDeleteRequest getAttributeSourceMetadataDeleteRequest() {
      AttributeSourceMetadataDeleteRequest.Builder builder =
          AttributeSourceMetadataDeleteRequest.newBuilder();
      Config filterConfig = config.getConfig("filter");
      if (filterConfig.hasPath("source")) {
        builder.setSource(AttributeSource.valueOf(filterConfig.getString("source")));
      }
      return builder.build();
    }
  }
}
