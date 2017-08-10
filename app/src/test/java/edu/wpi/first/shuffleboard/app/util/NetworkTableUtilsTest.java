package edu.wpi.first.shuffleboard.app.util;

import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.tables.ITable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkTableUtilsTest {

  @Before
  public void setUp() {
    NetworkTableUtils.shutdown();
  }

  @After
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

    for (int flag : flags) {
      // Make sure that n == n
      assertTrue(NetworkTableUtils.flagMatches(flag, flag));

      for (int f2 : flags) {
        // and that (n | m) == n
        assertTrue(NetworkTableUtils.flagMatches(flag | f2, flag));
      }
    }
  }

  @Test
  public void testOldMetadata() {
    String key = "/~metadata~";
    assertTrue(NetworkTableUtils.isMetadata(key));
  }

  @Test
  public void testOldMetadataSubkey() {
    String key = "/table/~metadata~";
    assertTrue(NetworkTableUtils.isMetadata(key));
  }

  @Test
  public void testOldMetadataSubtable() {
    String key = "/root/~metadata~/subkey";
    assertTrue(NetworkTableUtils.isMetadata(key));
  }

  @Test
  public void testNewMetadata() {
    String key = "/.metadata";
    assertTrue(NetworkTableUtils.isMetadata(key));
  }

  @Test
  public void testNewMetadataSubkey() {
    String key = "/table/.metadata";
    assertTrue(NetworkTableUtils.isMetadata(key));
  }

  @Test
  public void testNewMetadataSubtable() {
    String key = "/root/.metadata/key";
    assertTrue(NetworkTableUtils.isMetadata(key));
  }

}