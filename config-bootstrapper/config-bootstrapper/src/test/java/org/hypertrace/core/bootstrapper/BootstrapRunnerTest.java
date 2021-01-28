package org.hypertrace.core.bootstrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hypertrace.core.attribute.service.client.AttributeServiceClient;
import org.hypertrace.core.bootstrapper.commands.Command;
import org.hypertrace.core.bootstrapper.commands.CommandType;
import org.hypertrace.core.bootstrapper.dao.ConfigBootstrapStatusDao;
import org.hypertrace.core.bootstrapper.model.ConfigBootstrapStatus;
import org.hypertrace.entity.type.service.client.EntityTypeServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reflections.Reflections;

public class BootstrapRunnerTest {

  @Mock private ConfigBootstrapStatusDao configBootstrapStatusDao;
  @Mock private BootstrapContext bootstrapContext;
  @Mock private AttributeServiceClient attributesServiceClient;
  @Mock private EntityTypeServiceClient entityTypeServiceClient;
  private final String RESOURCE_DIR = "config-bootstrapper-test";
  private final String COMMAND_CONF_RESOURCE_DIR = RESOURCE_DIR + "/command-conf";
  private final String SINGLE_COMMAND_CONF_RESOURCE_DIR = COMMAND_CONF_RESOURCE_DIR + "/hypertrace-core";

  @BeforeEach
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(bootstrapContext.getAttributesServiceClient()).thenReturn(attributesServiceClient);
    when(bootstrapContext.getEntityTypeServiceClient()).thenReturn(entityTypeServiceClient);
  }

  @Test
  public void testUpgrade() {
    // Test upgrade - first run
    TestCommand.additions = 0;
    BootstrapRunner bootstrapRunner = new BootstrapRunner(configBootstrapStatusDao);
    when(configBootstrapStatusDao.upsertConfigBootstrapStatus(any())).thenReturn(true);
    when(configBootstrapStatusDao.getConfigBootstrapStatus(any())).thenReturn(null);
    String resourcesPath =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource(RESOURCE_DIR)
            .getPath();
    String resourceConf = Thread.currentThread()
        .getContextClassLoader()
        .getResource(SINGLE_COMMAND_CONF_RESOURCE_DIR)
        .getPath();;
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
              "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--upgrade"
            }));

    ArgumentCaptor<ConfigBootstrapStatus> bootstrapStatusCaptor =
        ArgumentCaptor.forClass(ConfigBootstrapStatus.class);
    verify(configBootstrapStatusDao).upsertConfigBootstrapStatus(bootstrapStatusCaptor.capture());
    ConfigBootstrapStatus configBootstrapStatus = bootstrapStatusCaptor.getValue();
    Assertions.assertEquals(1, configBootstrapStatus.getVersion());
    Assertions.assertEquals("test", configBootstrapStatus.getName());
    Assertions.assertEquals(1, TestCommand.additions);

    // Try rerunning without changing the bootstrap status
    when(configBootstrapStatusDao.getConfigBootstrapStatus(any()))
        .thenReturn(configBootstrapStatus);
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
              "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--upgrade"
            }));
    Assertions.assertEquals(1, TestCommand.additions);

    // Change checksum of the file and try again
    when(configBootstrapStatusDao.getConfigBootstrapStatus(any()))
        .thenReturn(
            new ConfigBootstrapStatus(
                configBootstrapStatus.getVersion(),
                configBootstrapStatus.getName(),
                "some other checksum",
                configBootstrapStatus.getStatus(),
                configBootstrapStatus.getRollbackConfig()));
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
              "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--upgrade"
            }));
    Assertions.assertEquals(2, TestCommand.additions);
  }

  @Test
  public void testRollback() {
    // Setup
    TestCommand.additions = 0;
    BootstrapRunner bootstrapRunner = new BootstrapRunner(configBootstrapStatusDao);
    when(configBootstrapStatusDao.upsertConfigBootstrapStatus(any())).thenReturn(true);
    when(configBootstrapStatusDao.getConfigBootstrapStatus(any())).thenReturn(null);
    String resourcesPath =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource(RESOURCE_DIR)
            .getPath();
    String resourceConf = Thread.currentThread()
        .getContextClassLoader()
        .getResource(SINGLE_COMMAND_CONF_RESOURCE_DIR)
        .getPath();;
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
                "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--upgrade"
            }));

    ArgumentCaptor<ConfigBootstrapStatus> bootstrapStatusCaptor =
        ArgumentCaptor.forClass(ConfigBootstrapStatus.class);
    verify(configBootstrapStatusDao).upsertConfigBootstrapStatus(bootstrapStatusCaptor.capture());
    ConfigBootstrapStatus configBootstrapStatus = bootstrapStatusCaptor.getValue();
    Assertions.assertEquals(1, configBootstrapStatus.getVersion());
    Assertions.assertEquals("test", configBootstrapStatus.getName());
    Assertions.assertEquals(
        ConfigBootstrapStatus.Status.SUCCEEDED, configBootstrapStatus.getStatus());
    Assertions.assertEquals(1, TestCommand.additions);

    // Test rollback
    when(configBootstrapStatusDao.getConfigBootstrapStatusGreaterThan(0))
        .thenReturn(List.of(configBootstrapStatus));
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
              "-c", resourcesPath + "/application.conf", "-C", resourcesPath, "--rollback", "0"
            }));
    configBootstrapStatus = bootstrapStatusCaptor.getValue();
    Assertions.assertEquals(
        ConfigBootstrapStatus.Status.ROLLED_BACK, configBootstrapStatus.getStatus());
    Assertions.assertEquals(1, TestCommand.deletions);
  }

  @Test
  public void testValidate() {
    TestCommand.validations = 0;
    BootstrapRunner bootstrapRunner = new BootstrapRunner(configBootstrapStatusDao);
    String resourcesPath =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource(RESOURCE_DIR)
            .getPath();
    String resourceConf = Thread.currentThread()
        .getContextClassLoader()
        .getResource(SINGLE_COMMAND_CONF_RESOURCE_DIR)
        .getPath();;
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
                "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--validate"
            }));

    Assertions.assertEquals(1, TestCommand.validations);
  }

  @Test
  public void testValidateCommandAnnotations() {
    Reflections reflections = new Reflections("org.hypertrace");
    // Verify that every class annotated with CommandType extends Command class
    Map<String, Class> commandTypeToClassMap =
        reflections.getTypesAnnotatedWith(CommandType.class).stream()
            .collect(
                Collectors.toMap(
                    clazz -> clazz.getAnnotation(CommandType.class).type(), Function.identity()));
    Assertions.assertFalse(commandTypeToClassMap.isEmpty());
    commandTypeToClassMap
        .values()
        .forEach(clazz -> Assertions.assertEquals(Command.class, clazz.getSuperclass()));

    // Verify that every class extending Command class has a CommandType annotation
    Set<Class<? extends Command>> commandClassesSet = reflections.getSubTypesOf(Command.class);
    Assertions.assertFalse(commandClassesSet.isEmpty());
    Set<String> commandTypes =
        commandClassesSet.stream()
            .map(commandClass -> commandClass.getAnnotation(CommandType.class))
            .filter(Objects::nonNull)
            .map(CommandType::type)
            .collect(Collectors.toSet());
    Assertions.assertEquals(
        commandClassesSet.size(),
        commandTypes.size(),
        String.format("commandClasses:%s   commandTypes:%s", commandClassesSet, commandTypes));
  }

  @Test
  public void testUpgrade_confWith_same_version_name() {
    // Test upgrade - first run
    TestCommand.additions = 0;
    BootstrapRunner bootstrapRunner = new BootstrapRunner(configBootstrapStatusDao);
    when(configBootstrapStatusDao.upsertConfigBootstrapStatus(any())).thenReturn(true);
    when(configBootstrapStatusDao.getConfigBootstrapStatus(any())).thenReturn(null);
    String resourcesPath =
        Thread.currentThread()
            .getContextClassLoader()
            .getResource(RESOURCE_DIR)
            .getPath();
    String resourceConf = Thread.currentThread()
        .getContextClassLoader()
        .getResource(COMMAND_CONF_RESOURCE_DIR)
        .getPath();;
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
                "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--upgrade"
            }));

    ArgumentCaptor<ConfigBootstrapStatus> bootstrapStatusCaptor =
        ArgumentCaptor.forClass(ConfigBootstrapStatus.class);
    verify(configBootstrapStatusDao).upsertConfigBootstrapStatus(bootstrapStatusCaptor.capture());
    ConfigBootstrapStatus configBootstrapStatus = bootstrapStatusCaptor.getValue();
    Assertions.assertEquals(1, configBootstrapStatus.getVersion());
    Assertions.assertEquals("test", configBootstrapStatus.getName());
    Assertions.assertEquals(2, TestCommand.additions);

    // Try rerunning without changing the bootstrap status
    when(configBootstrapStatusDao.getConfigBootstrapStatus(any()))
        .thenReturn(configBootstrapStatus);
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
                "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--upgrade"
            }));
    Assertions.assertEquals(2, TestCommand.additions);

    // Change checksum of the file and try again
    when(configBootstrapStatusDao.getConfigBootstrapStatus(any()))
        .thenReturn(
            new ConfigBootstrapStatus(
                configBootstrapStatus.getVersion(),
                configBootstrapStatus.getName(),
                "some other checksum",
                configBootstrapStatus.getStatus(),
                configBootstrapStatus.getRollbackConfig()));
    bootstrapRunner.execute(
        BootstrapArgs.from(
            new String[] {
                "-c", resourcesPath + "/application.conf", "-C", resourceConf, "--upgrade"
            }));
    Assertions.assertEquals(4, TestCommand.additions);
  }
}
