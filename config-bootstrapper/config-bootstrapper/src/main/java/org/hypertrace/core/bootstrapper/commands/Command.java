package org.hypertrace.core.bootstrapper.commands;

import com.typesafe.config.Config;
import org.hypertrace.core.bootstrapper.BootstrapContext;

/** Abstract class for all commands supported by the config bootstrapper */
public abstract class Command {
  protected static final String TENANT_ID_KEY = "tenantId";
  private static final String ROOT_TENANT_ID = "__root";
  protected BootstrapContext bootstrapContext;

  public Command(BootstrapContext bootstrapContext) {
    this.bootstrapContext = bootstrapContext;
  }

  /** Abstract class for any operation supported by a command */
  public abstract static class Operation {
    protected Config config;

    public Operation(Config config) {
      this.config = config;
    }

    public void validate() {}

    protected String getTenantId() {
      return config.hasPath(TENANT_ID_KEY) ? config.getString(TENANT_ID_KEY) : ROOT_TENANT_ID;
    }

    public abstract void execute();
  }
}
