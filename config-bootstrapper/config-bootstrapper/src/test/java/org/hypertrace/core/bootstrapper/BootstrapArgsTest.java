package org.hypertrace.core.bootstrapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BootstrapArgsTest {

  @Test
  public void testBootstrapArgsValid() {
    {
      BootstrapArgs bootstrapArgs =
          BootstrapArgs.from(
              new String[] {"-c", "/tmp/application.conf", "-C", "/tmp", "--upgrade"});
      Assertions.assertNotNull(bootstrapArgs);
      Assertions.assertTrue(bootstrapArgs.isUpgrade());
      Assertions.assertFalse(bootstrapArgs.isRollback());
      Assertions.assertEquals("/tmp", bootstrapArgs.getCommandResource());
      Assertions.assertEquals("/tmp/application.conf", bootstrapArgs.getConfigFile());
    }

    {
      BootstrapArgs bootstrapArgs =
          BootstrapArgs.from(
              new String[] {"-c", "/tmp/application.conf", "-C", "/tmp", "--rollback", "1"});
      Assertions.assertNotNull(bootstrapArgs);
      Assertions.assertEquals(1, bootstrapArgs.getRollbackVersion());
      Assertions.assertTrue(bootstrapArgs.isRollback());
      Assertions.assertFalse(bootstrapArgs.isUpgrade());
      Assertions.assertEquals("/tmp", bootstrapArgs.getCommandResource());
      Assertions.assertEquals("/tmp/application.conf", bootstrapArgs.getConfigFile());
    }
  }

  @Test
  public void testBootstrapArgsInvalid1() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> BootstrapArgs.from(new String[] {"-c", "/tmp/application.conf", "-C", "/tmp"}));
  }

  @Test
  public void testBootstrapArgsInvalid2() {
    Assertions.assertThrows(
        RuntimeException.class,
        () ->
            BootstrapArgs.from(
                new String[] {
                  "-c", "/tmp/application.conf", "-C", "/tmp", "--upgrade", "--rollback"
                }));
  }

  @Test
  public void testBootstrapArgsInvalid3() {
    Assertions.assertThrows(
        RuntimeException.class, () -> BootstrapArgs.from(new String[] {"-C", "/tmp", "--upgrade"}));
  }

  @Test
  public void testBootstrapArgsInvalid4() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> BootstrapArgs.from(new String[] {"-c", "/tmp/application.conf", "--upgrade"}));
  }
}
