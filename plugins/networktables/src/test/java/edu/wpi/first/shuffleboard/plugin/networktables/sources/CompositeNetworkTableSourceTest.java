package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.MapData;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.testfx.framework.junit5.ApplicationTest;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// Note: we do a boatload of repetitions on these tests to make sure ntcore listeners trigger
// We also use sleep calls because NetworkTableInstance.waitForEntryListenerQueue() fails about 20% of the time
// which makes the tests break
@Tag("UI")
public class CompositeNetworkTableSourceTest extends ApplicationTest {

  private static final String tableName = "/CompositeNetworkTableSourceTest";

  private final NetworkTableInstance ntInstance = NetworkTableInstance.getDefault();

  private void waitForNtEvents() {
    if (!ntInstance.waitForEntryListenerQueue(0.5)) {
      fail("Timed out while waiting for entry listeners to fire");
    }
  }

  @BeforeAll
  public static void clinit() {
    NetworkTableSourceType.setInstance(new NetworkTableSourceType(new NetworkTablesPlugin()));
  }

  @BeforeEach
  public void setUp() {
    AsyncUtils.setAsyncRunner(Runnable::run);
    NetworkTableUtils.shutdown();
    waitForNtEvents();
  }

  @AfterEach
  public void tearDown() {
    NetworkTableUtils.shutdown();
    waitForNtEvents();
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
  }

  @RepeatedTest(10)
  public void testInactiveByDefault() {
    CompositeNetworkTableSource<MapData> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    assertFalse(source.isActive());
    assertTrue(source.getData().isEmpty());
    source.close();
  }

  @RepeatedTest(10)
  public void testDataUpdates() {
    final CompositeNetworkTableSource<MapData> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    source.setConnected(true);
    final String key = "key1";
    final NetworkTableInstance inst = NetworkTableInstance.getDefault();

    inst.getTable(tableName).getEntry(key).setString("value1");
    waitForNtEvents();
    assertEquals("value1", source.getData().get(key));

    inst.getTable(tableName).getEntry(key).setString("value2");
    waitForNtEvents();
    assertEquals("value2", source.getData().get(key));
    source.close();
  }

  @RepeatedTest(10)
  public void testTypeDetectedCorrectly() {
    final CompositeNetworkTableSource<?> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    final NetworkTableInstance inst = NetworkTableInstance.getDefault();

    inst.getTable(tableName).getEntry(".type").setString("Map");
    waitForNtEvents();
    assertTrue(source.isActive(), "Source not active");
    source.close();
  }

  @RepeatedTest(10)
  public void testUpdatesCorrectEntry() {
    // given
    final CompositeNetworkTableSource<MapData> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    final NetworkTableInstance inst = NetworkTableInstance.getDefault();
    final NetworkTable table = inst.getTable(tableName);
    final NetworkTableEntry entry = table.getEntry("testUpdatesCorrectEntry");

    // when
    source.setData(new MapData(ImmutableMap.of("testUpdatesCorrectEntry", "It does!")));
    waitForNtEvents();

    // then
    assertAll(
        () -> assertThat("Unexpected keys: " + table.getKeys(), table.getKeys(), hasItem("testUpdatesCorrectEntry")),
        () -> assertEquals(NetworkTableType.kString, entry.getValue().getType()),
        () -> assertEquals("It does!", entry.getValue().getValue())
    );

    source.close();
  }
}
