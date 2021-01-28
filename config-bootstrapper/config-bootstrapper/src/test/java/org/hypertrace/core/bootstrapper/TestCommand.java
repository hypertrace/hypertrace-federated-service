package org.hypertrace.core.bootstrapper;

import com.typesafe.config.Config;
import org.hypertrace.core.bootstrapper.commands.Command;
import org.hypertrace.core.bootstrapper.commands.CommandType;

@CommandType(type = "Test")
public class TestCommand extends Command {
  static int validations = 0;
  static int additions = 0;
  static int deletions = 0;

  public TestCommand(BootstrapContext bootstrapContext) {
    super(bootstrapContext);
  }

  public void validate() {
    validations++;
  }

  public void add() {
    additions++;
  }

  public void delete() {
    deletions++;
  }

  public class ADD extends Operation {

    public ADD(Config config) {
      super(config);
    }

    @Override
    public void validate() {
      new TestCommand(null).validate();
    }

    @Override
    public void execute() {
      new TestCommand(null).add();
    }
  }

  public class DELETE extends Operation {

    public DELETE(Config config) {
      super(config);
    }

    @Override
    public void execute() {
      new TestCommand(null).delete();
    }
  }
}
