package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.MapData;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;

import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositeNetworkTableSourceTest {

  private static final String tableName = "/CompositeNetworkTableSourceTest";

  @BeforeAll
  public static void clinit() {
    NetworkTableSourceType.setInstance(new NetworkTableSourceType(new NetworkTablesPlugin()));
  }

  @BeforeEach
  public void setUp() {
    AsyncUtils.setAsyncRunner(Runnable::run);
    NetworkTableUtils.shutdown();
    NetworkTableInstance.getDefault().waitForEntryListenerQueue(-1.0);
  }

  @AfterEach
  public void tearDown() {
    NetworkTableUtils.shutdown();
    NetworkTableInstance.getDefault().waitForEntryListenerQueue(-1.0);
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
  }

  @Test
  public void testInactiveByDefault() {
    CompositeNetworkTableSource<MapData> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    assertFalse(source.isActive());
    assertTrue(source.getData().isEmpty());
    source.close();
  }

  @Test
  public void testDataUpdates() {
    final CompositeNetworkTableSource<MapData> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    source.setConnected(true);
    final String key = "key1";
    final NetworkTableInstance inst = NetworkTableInstance.getDefault();

    inst.getTable(tableName).getEntry(key).setString("value1");
    inst.waitForEntryListenerQueue(-1.0);
    assertEquals("value1", source.getData().get(key));

    inst.getTable(tableName).getEntry(key).setString("value2");
    inst.waitForEntryListenerQueue(-1.0);
    assertEquals("value2", source.getData().get(key));
    source.close();
  }

  @Test
  public void testTypeDetectedCorrectly() {
    final CompositeNetworkTableSource<?> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    final NetworkTableInstance inst = NetworkTableInstance.getDefault();

    inst.getTable(tableName).getEntry(".type").setString("Map");
    inst.waitForEntryListenerQueue(-1.0);
    assertTrue(source.isActive(), "Source not active");
    source.close();
  }
}
