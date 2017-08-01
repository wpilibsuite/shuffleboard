package edu.wpi.first.shuffleboard.util;

import edu.wpi.first.wpilibj.tables.ITable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class NetworkTableUtilsTest {

  @BeforeEach
  public void setUp() {
    NetworkTableUtils.shutdown();
  }

  @AfterEach
  public void tearDown() {
    NetworkTableUtils.shutdown();
  }

  @Test
  public void testSimpleKey() {
    String simple = "a simple key";
    assertEquals(simple, NetworkTableUtils.simpleKey(simple));
  }

  @Test
  public void testSimplifyNestedKey() {
    String key = "/a/complex/key";
    assertEquals("key", NetworkTableUtils.simpleKey(key));
  }

  @Test
  public void testNormalizeSimple() {
    String key = "a key";
    assertEquals(key, NetworkTableUtils.normalizeKey(key, false));
  }

  @Test
  public void testNormalizeComplex() {
    String key = "//lots////of///slashes";
    assertEquals("/lots/of/slashes", NetworkTableUtils.normalizeKey(key));
  }

  @Test
  public void testIsDelete() {
    assertTrue(NetworkTableUtils.isDelete(ITable.NOTIFY_DELETE));
    assertTrue(NetworkTableUtils.isDelete(ITable.NOTIFY_IMMEDIATE | ITable.NOTIFY_DELETE));
    assertTrue(NetworkTableUtils.isDelete(0xFF));
    assertFalse(NetworkTableUtils.isDelete(0x00));
  }

  @Test
  public void testFlags() {
    final int[] flags = {
        ITable.NOTIFY_IMMEDIATE,
        ITable.NOTIFY_LOCAL,
        ITable.NOTIFY_NEW,
        ITable.NOTIFY_DELETE,
        ITable.NOTIFY_UPDATE,
        ITable.NOTIFY_FLAGS
    };

    final Set<Executable> assertions = new HashSet<>();
    for (int flag : flags) {
      // Make sure that n == n
      assertions.add(() -> assertTrue(NetworkTableUtils.flagMatches(flag, flag)));

      for (int f2 : flags) {
        // and that (n | m) == n
        assertions.add(() -> assertTrue(NetworkTableUtils.flagMatches(flag | f2, flag)));
      }
    }
    assertAll(assertions.stream());
  }

}
