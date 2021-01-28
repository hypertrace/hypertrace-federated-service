package org.hypertrace.core.query.service.pinot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PinotQueryTest {

  @Test
  public void testPinotQuery() {
    final AdhocPinotQuery q1 = new AdhocPinotQuery("query1", null);
    q1.setQuery("q1");
    final AdhocPinotQuery q2 = new AdhocPinotQuery("query2", null);
    q2.setQuery("q2");
    final AdhocPinotQuery q3 = new AdhocPinotQuery("query2", null);
    q3.setQuery("q1");
    Assertions.assertNotEquals(q2, q1);
    Assertions.assertNotEquals(q3, q1);
    Assertions.assertNotEquals(q3, q2);
    Assertions.assertNotEquals(q1, q2);
    Assertions.assertNotEquals(q2, q3);
    q3.setQuery("q2");
    Assertions.assertEquals(q2, q3);
    Assertions.assertEquals(q3, q2);
  }
}
