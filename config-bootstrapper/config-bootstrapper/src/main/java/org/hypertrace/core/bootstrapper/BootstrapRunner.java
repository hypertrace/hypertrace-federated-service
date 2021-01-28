package org.hypertrace.core.bootstrapper;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.hypertrace.core.bootstrapper.commands.Command;
import org.hypertrace.core.bootstrapper.commands.Command.Operation;
import org.hypertrace.core.bootstrapper.commands.CommandType;
import org.hypertrace.core.bootstrapper.dao.ConfigBootstrapStatusDao;
import org.hypertrace.core.bootstrapper.model.ConfigBootstrapStatus;
import org.hypertrace.core.bootstrapper.model.ConfigBootstrapStatusKey;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main runner class that parses the bootstrap arguments and performs an upgrade or rollback
 */
public class BootstrapRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapRunner.class);
  private final ConfigBootstrapStatusDao configBootstrapStatusDao;
  private final Map<String, Class> commandNameToClassMap;

  public BootstrapRunner(ConfigBootstrapStatusDao configBootstrapStatusDao) {
    this.configBootstrapStatusDao = configBootstrapStatusDao;
    this.commandNameToClassMap =
        new Reflections("org.hypertrace")
            .getTypesAnnotatedWith(CommandType.class).stream()
            .collect(
                Collectors.toMap(
                    clazz -> clazz.getAnnotation(CommandType.class).type(),
                    Function.identity()));
  }

  /**
   * Executes the bootstrap commands based on the passed arguments
   */
  public void execute(BootstrapArgs bootstrapArgs) {
    BootstrapContext bootstrapContext =
        BootstrapContext.buildFrom(
            ConfigFactory.parseFile(new File(bootstrapArgs.getConfigFile())).resolve());
    try {
      if (bootstrapArgs.isValidate()) {
        validate(bootstrapContext, bootstrapArgs);
      }
      if (bootstrapArgs.isUpgrade()) {
        upgrade(bootstrapContext, bootstrapArgs);
      }
      if (bootstrapArgs.isRollback()) {
        rollback(bootstrapContext, bootstrapArgs);
      }
    } finally {
      bootstrapContext.close();
    }
  }

  private void validate(BootstrapContext bootstrapContext, BootstrapArgs bootstrapArgs) {
    LOGGER.info(
        "Starting validate with args:{} at time:{}", bootstrapArgs, System.currentTimeMillis());
    File commandsResource = new File(bootstrapArgs.getCommandResource());
    File[] commandFiles =
        (commandsResource.isFile()) ? new File[]{commandsResource} :
            FileUtils.listFiles(commandsResource, null, true).toArray(File[]::new);
    Map<ConfigBootstrapStatusKey, Config> bootstrapStatusKeyConfigMap =
        groupConfigsByKey(commandFiles);
    for (Entry<ConfigBootstrapStatusKey, Config> configEntry :
        bootstrapStatusKeyConfigMap.entrySet()) {
      getCommandsConfigStreamForUpgrade(configEntry.getValue())
          .forEach(
              commandConfig ->
                  executeCommand(bootstrapContext, commandConfig, Operation::validate));
    }
  }

  private void upgrade(BootstrapContext bootstrapContext, BootstrapArgs bootstrapArgs) {
    LOGGER.info(
        "Starting upgrade with args:{} at time:{}", bootstrapArgs, System.currentTimeMillis());
    File commandsResource = new File(bootstrapArgs.getCommandResource());
    File[] commandFiles =
        (commandsResource.isFile()) ? new File[]{commandsResource} :
            FileUtils.listFiles(commandsResource, null, true).toArray(File[]::new);
    for (File f : commandFiles) {
      LOGGER.info("Reading config file:{}", f.getAbsolutePath());
    }
    Map<ConfigBootstrapStatusKey, Config> bootstrapStatusKeyConfigMap =
        groupConfigsByKey(commandFiles);
    for (Entry<ConfigBootstrapStatusKey, Config> configEntry :
        bootstrapStatusKeyConfigMap.entrySet()) {
      // get the status for each key and upgrade only if needed
      ConfigBootstrapStatus configBootstrapStatus =
          configBootstrapStatusDao.getConfigBootstrapStatus(configEntry.getKey());
      ConfigBootstrapStatusKey configKey = configEntry.getKey();
      String checksum = DigestUtils.sha1Hex(configEntry.getValue().toString());

      // apply the config only if it hasn't been applied
      // or if the config has changed
      // or the status is not SUCCESS
      if (configBootstrapStatus == null
          || !configBootstrapStatus.getFileChecksum().equals(checksum)
          || !configBootstrapStatus.getStatus().equals(ConfigBootstrapStatus.Status.SUCCEEDED)) {

        // Execute the commands in the config file
        getCommandsConfigStreamForUpgrade(configEntry.getValue())
            .forEach(
                commandConfig ->
                    executeCommand(bootstrapContext, commandConfig, Operation::execute));

        // Update db with status and rollback config
        List<ConfigValue> rollbackConfigList =
            getCommandsConfigStreamForRollback(configEntry.getValue())
                .map(c -> ConfigValueFactory.fromAnyRef(c.root()))
                .collect(Collectors.toList());

        // Reverse the rollback commands before persisting in DB
        Collections.reverse(rollbackConfigList);
        Config rollbackConfig =
            ConfigFactory.parseMap(Map.of(BootstrapConstants.ROLLBACK, rollbackConfigList));
        ConfigBootstrapStatus status =
            new ConfigBootstrapStatus(
                configKey.getVersion(),
                configKey.getName(),
                checksum,
                ConfigBootstrapStatus.Status.SUCCEEDED,
                rollbackConfig.root().render(ConfigRenderOptions.concise()));
        configBootstrapStatusDao.upsertConfigBootstrapStatus(status);
      }
      LOGGER.info("Upgrade done for config:{}", configEntry.getKey());
    }
    LOGGER.info("Upgrade complete at time:{}", System.currentTimeMillis());
  }

  private void rollback(BootstrapContext bootstrapContext, BootstrapArgs bootstrapArgs) {
    LOGGER.info(
        "Starting rollback with args:{} at time:{}", bootstrapArgs, System.currentTimeMillis());
    // Fetch all applied configs greater than the version to rollback to
    List<ConfigBootstrapStatus> configBootstrapStatusList =
        configBootstrapStatusDao.getConfigBootstrapStatusGreaterThan(
            bootstrapArgs.getRollbackVersion());

    // Fetch the rollback config from DB and execute rollback commands
    configBootstrapStatusList.forEach(
        configBootstrapStatus -> {
          Config rollbackConfig =
              ConfigFactory.parseString(configBootstrapStatus.getRollbackConfig());
          if (rollbackConfig.hasPath(BootstrapConstants.ROLLBACK)) {
            List<? extends Config> rollbackConfigList =
                rollbackConfig.getConfigList(BootstrapConstants.ROLLBACK);
            rollbackConfigList.forEach(
                config -> executeCommand(bootstrapContext, config, Operation::execute));
          }

          // Update the status in DB
          configBootstrapStatus.setStatus(ConfigBootstrapStatus.Status.ROLLED_BACK);
          configBootstrapStatusDao.upsertConfigBootstrapStatus(configBootstrapStatus);
        });
    LOGGER.info("Rollback complete at time:{}", System.currentTimeMillis());
  }

  private Map<ConfigBootstrapStatusKey, Config> groupConfigsByKey(File[] commandFiles) {
    return Arrays.stream(Objects.requireNonNull(commandFiles))
        .filter(File::isFile)
        .map(ConfigFactory::parseFile)
        .filter(
            config ->
                config.hasPath(BootstrapConstants.VERSION)
                    && config.hasPath(BootstrapConstants.NAME))
        .collect(
            Collectors.toMap(
                config ->
                    new ConfigBootstrapStatusKey(
                        config.getInt(BootstrapConstants.VERSION),
                        config.getString(BootstrapConstants.NAME)),
                Function.identity(),
                (c1, c2) -> {
                  List<? extends ConfigObject> configList =
                      Stream.concat(
                          c1.getObjectList(BootstrapConstants.COMMANDS).stream(),
                          c2.getObjectList(BootstrapConstants.COMMANDS).stream())
                          .collect(Collectors.toList());
                  return ConfigFactory.parseMap(Map.of(BootstrapConstants.COMMANDS, configList));
                },
                TreeMap::new));
  }

  private Stream<? extends Config> getCommandsConfigStreamForUpgrade(Config config) {
    return getCommandsConfigStream(config, BootstrapConstants.UPGRADE);
  }

  private Stream<? extends Config> getCommandsConfigStreamForRollback(Config config) {
    return getCommandsConfigStream(config, BootstrapConstants.ROLLBACK);
  }

  private Stream<? extends Config> getCommandsConfigStream(Config conf, String subCommand) {
    try {
      return conf.getConfigList(BootstrapConstants.COMMANDS).stream()
          .flatMap(commandConfig -> commandConfig.getConfigList(subCommand).stream());
    } catch (ConfigException.Missing configMissingException) {
      LOGGER.warn("No commands in the config:{}", conf);
      return Stream.empty();
    }
  }

  private void executeCommand(
      BootstrapContext bootstrapContext,
      Config commandConfig,
      Consumer<Operation> operationFunction) {
    try {
      String commandType = commandConfig.getString("type");
      String commandAction = commandConfig.getString("action");
      Config commandData = commandConfig.getConfig("data");

      Class<?> enclosingClass = commandNameToClassMap.get(commandType);
      if (enclosingClass == null) {
        LOGGER.error("Command class not found for commandType:{}", commandType);
        throw new RuntimeException(
            String.format("Command class not found for commandType:%s", commandType));
      }
      Object enclosingInstance =
          enclosingClass
              .getDeclaredConstructor(BootstrapContext.class)
              .newInstance(bootstrapContext);
      assert enclosingInstance instanceof Command;

      Class<?> innerClass = Class.forName(enclosingClass.getName() + "$" + commandAction);
      Constructor<?> ctor = innerClass.getDeclaredConstructor(enclosingClass, Config.class);
      Object innerInstance = ctor.newInstance(enclosingInstance, commandData);
      assert innerInstance instanceof Operation;
      operationFunction.accept((Operation) innerInstance);
    } catch (Exception e) {
      LOGGER.error("Error executing command:{}", commandConfig);
      throw new RuntimeException(e);
    }
  }
}
