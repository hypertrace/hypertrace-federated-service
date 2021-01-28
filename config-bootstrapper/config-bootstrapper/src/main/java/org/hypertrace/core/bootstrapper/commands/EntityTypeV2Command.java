package org.hypertrace.core.bootstrapper.commands;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.hypertrace.core.bootstrapper.BootstrapContext;
import org.hypertrace.entity.type.client.EntityTypeServiceClient;
import org.hypertrace.entity.type.service.v2.DeleteEntityTypesRequest;
import org.hypertrace.entity.type.service.v2.EntityType;

/** Command class for EntityType 2 API operations(ADD/DELETE) */
@CommandType(type = "EntityTypeV2")
public class EntityTypeV2Command extends Command {
  private static final Parser PARSER = JsonFormat.parser().ignoringUnknownFields();

  public EntityTypeV2Command(BootstrapContext bootstrapContext) {
    super(bootstrapContext);
  }

  public class ADD extends Operation {
    private static final String ENTITY_TYPES_PATH = "entityTypes";

    public ADD(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      EntityTypeServiceClient entityTypeServiceClient =
          bootstrapContext.getEntityTypeServiceClientV2();
      config.getConfigList(ENTITY_TYPES_PATH).stream()
          .map(
              entityTypeConfig -> {
                try {
                  EntityType.Builder entityTypeBuilder = EntityType.newBuilder();
                  String configString =
                      entityTypeConfig.root().render(ConfigRenderOptions.concise().setJson(true));
                  PARSER.merge(configString, entityTypeBuilder);
                  return entityTypeBuilder.build();
                } catch (InvalidProtocolBufferException ex) {
                  throw new RuntimeException(
                      String.format("Error creating EntityType from config:%s", entityTypeConfig),
                      ex);
                }
              })
          .forEach(
              entityType -> entityTypeServiceClient.upsertEntityType(getTenantId(), entityType));
    }
  }

  public class DELETE extends Operation {

    public DELETE(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      bootstrapContext.getEntityTypeServiceClientV2()
          .deleteEntityTypes(getTenantId(), getDeleteEntityTypeRequest());
    }

    private DeleteEntityTypesRequest getDeleteEntityTypeRequest() {
      DeleteEntityTypesRequest.Builder entityTypeRequestBuilder = DeleteEntityTypesRequest.newBuilder();
      Config filterConfig = config.getConfig("filter");
      try {
        String configString =
            filterConfig.root().render(ConfigRenderOptions.concise().setJson(true));
        PARSER.merge(configString, entityTypeRequestBuilder);
        return entityTypeRequestBuilder.build();
      } catch (InvalidProtocolBufferException ex) {
        throw new RuntimeException(
            String.format("Error creating EntityTypeFilter from config:%s", entityTypeRequestBuilder),
            ex);
      }
    }
  }
}

