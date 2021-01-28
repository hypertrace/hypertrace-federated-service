package org.hypertrace.core.query.service.pinot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PinotUtilsTest {

  @Test
  public void testZkPath() {
    Assertions.assertEquals(
        "localhost:2181/pinot", PinotUtils.getZkPath("localhost:2181", "pinot"));
    Assertions.assertEquals(
        "localhost:2181/pinot", PinotUtils.getZkPath("localhost:2181/", "pinot"));
    Assertions.assertEquals(
        "localhost:2181/pinot/myView", PinotUtils.getZkPath("localhost:2181/pinot", "myView"));
    Assertions.assertEquals(
        "localhost:2181/pinot/myView", PinotUtils.getZkPath("localhost:2181/pinot/", "myView"));
  }

  @Test
  public void testGetMetricName() {
    Assertions.assertEquals("span.event.view.handler",
        PinotUtils.getMetricName("span-event-view-handler"));

    Assertions.assertEquals("span.event.view.handler",
        PinotUtils.getMetricName("span.event-view-handler"));

    Assertions.assertEquals("span_event.view.handler",
        PinotUtils.getMetricName("span_event-view-handler"));
  }
}
