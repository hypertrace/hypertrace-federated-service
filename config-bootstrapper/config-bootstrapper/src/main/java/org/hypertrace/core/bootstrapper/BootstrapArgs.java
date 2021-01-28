package org.hypertrace.core.bootstrapper;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Encapsulates all the arguments passed to the config bootstrapper. Appropriate commands are
 * executed based on the passed in arguments
 */
public class BootstrapArgs {
  private String configFile;
  private String commandResource;
  private boolean upgrade;
  private boolean rollback;
  private boolean validate;
  private int rollbackVersion;

  private BootstrapArgs() {}

  public static BootstrapArgs from(String[] args) {
    BootstrapArgs bootstrapArgs = new BootstrapArgs();

    Option configFile =
        Option.builder("c")
            .longOpt("config")
            .hasArg()
            .argName("file")
            .desc("Path to config file for the bootstrapper")
            .required()
            .build();

    Option commandsResource =
        Option.builder("C")
            .longOpt("commands")
            .hasArg()
            .argName("file/folder")
            .desc("Path to folder/file containing commands")
            .required()
            .build();

    Option upgrade = Option.builder().longOpt("upgrade").desc("Performs an upgrade").build();

    Option validate =
        Option.builder().longOpt("validate").desc("Performs a validation of the configs").build();

    Option rollback =
        Option.builder()
            .longOpt("rollback")
            .hasArg()
            .desc("Performs a rollback to a specified version")
            .build();

    Options options = new Options();
    options.addOption(configFile);
    options.addOption(commandsResource);
    options.addOption(upgrade);
    options.addOption(validate);
    options.addOption(rollback);
    CommandLine commandLine;
    try {
      CommandLineParser parser = new DefaultParser();
      // parse the command line arguments
      commandLine = parser.parse(options, args);
    } catch (ParseException exp) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("<jar>", options);
      throw new RuntimeException("Parsing exception:", exp);
    }

    bootstrapArgs.setConfigFile(commandLine.getOptionValue("config"));
    bootstrapArgs.setCommandResource(commandLine.getOptionValue("commands"));
    bootstrapArgs.setUpgrade(commandLine.hasOption("upgrade"));
    bootstrapArgs.setValidate(commandLine.hasOption("validate"));
    boolean isRollback = commandLine.hasOption("rollback");
    if (isRollback) {
      bootstrapArgs.setRollback(true);
      bootstrapArgs.setRollbackVersion(Integer.parseInt(commandLine.getOptionValue("rollback")));
    }

    // validate before returning
    validate(bootstrapArgs);
    return bootstrapArgs;
  }

  private static void validate(BootstrapArgs bootstrapArgs) {
    if (!bootstrapArgs.isUpgrade() && !bootstrapArgs.isRollback() && !bootstrapArgs.isValidate()) {
      throw new RuntimeException(
          "One of --upgrade or --rollback or --validate should be provided in the args");
    }
    if (bootstrapArgs.isUpgrade() && bootstrapArgs.isRollback()) {
      throw new RuntimeException("Both upgrade and rollback cant be done together");
    }
  }

  public String getConfigFile() {
    return configFile;
  }

  public void setConfigFile(String configFile) {
    this.configFile = configFile;
  }

  public String getCommandResource() {
    return commandResource;
  }

  public void setCommandResource(String commandResource) {
    this.commandResource = commandResource;
  }

  public boolean isUpgrade() {
    return upgrade;
  }

  public void setUpgrade(boolean upgrade) {
    this.upgrade = upgrade;
  }

  public boolean isRollback() {
    return rollback;
  }

  public void setRollback(boolean rollback) {
    this.rollback = rollback;
  }

  public boolean isValidate() {
    return validate;
  }

  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  public int getRollbackVersion() {
    return rollbackVersion;
  }

  public void setRollbackVersion(int rollbackVersion) {
    this.rollbackVersion = rollbackVersion;
  }

  @Override
  public String toString() {
    return "BootstrapArgs{"
        + "configFile='"
        + configFile
        + '\''
        + ", commandResource='"
        + commandResource
        + '\''
        + ", upgrade="
        + upgrade
        + ", rollback="
        + rollback
        + ", validate="
        + validate
        + ", rollbackVersion="
        + rollbackVersion
        + '}';
  }
}
