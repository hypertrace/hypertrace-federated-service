package org.hypertrace.core.bootstrapper.commands;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.hypertrace.core.bootstrapper.BootstrapContext;
import org.hypertrace.entity.type.service.client.EntityTypeServiceClient;
import org.hypertrace.entity.type.service.v1.EntityRelationshipType;
import org.hypertrace.entity.type.service.v1.EntityRelationshipTypeFilter;

/** Command class for EntityRelationshipType operations(ADD/DELETE) */
@CommandType(type = "EntityRelationshipType")
public class EntityRelationshipTypeCommand extends Command {
  private static final Parser PARSER = JsonFormat.parser().ignoringUnknownFields();

  public EntityRelationshipTypeCommand(BootstrapContext bootstrapContext) {
    super(bootstrapContext);
  }

  public class ADD extends Operation {
    private static final String ENTITY_RELATIONSHIP_TYPES_PATH = "entityRelationshipTypes";

    public ADD(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      String tenantId = getTenantId();
      EntityTypeServiceClient entityTypeServiceClient =
          bootstrapContext.getEntityTypeServiceClient();
      config.getConfigList(ENTITY_RELATIONSHIP_TYPES_PATH).stream()
          .map(
              entityTypeConfig -> {
                try {
                  EntityRelationshipType.Builder entityTypeBuilder =
                      EntityRelationshipType.newBuilder();
                  String configString =
                      entityTypeConfig.root().render(ConfigRenderOptions.concise().setJson(true));
                  PARSER.merge(configString, entityTypeBuilder);
                  return entityTypeBuilder.build();
                } catch (InvalidProtocolBufferException ex) {
                  throw new RuntimeException(
                      String.format(
                          "Error creating EntityRelationshipType from config:%s", entityTypeConfig),
                      ex);
                }
              })
          .forEach(
              entityRelationshipType ->
                  entityTypeServiceClient.upsertEntityRelationshipType(
                      tenantId, entityRelationshipType));
    }
  }

  public class DELETE extends Operation {

    public DELETE(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      bootstrapContext
          .getEntityTypeServiceClient()
          .deleteEntityRelationshipTypes(getTenantId(), getEntityRelationshipTypeFilter());
    }

    private EntityRelationshipTypeFilter getEntityRelationshipTypeFilter() {
      EntityRelationshipTypeFilter.Builder entityRelationshipTypeFilterBuilder =
          EntityRelationshipTypeFilter.newBuilder();
      Config filterConfig = config.getConfig("filter");
      try {
        String configString =
            filterConfig.root().render(ConfigRenderOptions.concise().setJson(true));
        PARSER.merge(configString, entityRelationshipTypeFilterBuilder);
        return entityRelationshipTypeFilterBuilder.build();
      } catch (InvalidProtocolBufferException ex) {
        throw new RuntimeException(
            String.format(
                "Error creating EntityRelationshipTypeFilter from config:%s",
                entityRelationshipTypeFilterBuilder),
            ex);
      }
    }
  }
}
