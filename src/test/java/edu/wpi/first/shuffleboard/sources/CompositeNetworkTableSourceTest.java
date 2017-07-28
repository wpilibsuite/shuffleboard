package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.data.DataTypes;
import edu.wpi.first.shuffleboard.data.MapData;
import edu.wpi.first.shuffleboard.util.AsyncUtils;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static edu.wpi.first.shuffleboard.util.NetworkTableUtils.waitForNtcoreEvents;
import static org.junit.Assert.*;

public class CompositeNetworkTableSourceTest {

  private static final String tableName = "CompositeNetworkTableSourceTest";

  @Before
  public void setUp() {
    NetworkTableUtils.shutdown();
    AsyncUtils.setAsyncRunner(Runnable::run);
  }

  @After
  public void tearDown() {
    NetworkTableUtils.shutdown();
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
  }

  @Test
  public void testInactiveByDefault() {
    CompositeNetworkTableSource<MapData> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    assertFalse(source.isActive());
    assertTrue(source.getData().isEmpty());
  }

  @Ignore
  @Test
  public void testDataUpdates() {
    CompositeNetworkTableSource<MapData> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.Map);
    final String key = "key1";

    NetworkTable.getTable(tableName).putString(key, "value1");
    waitForNtcoreEvents();
    assertEquals("value1", source.getData().get(key));

    NetworkTable.getTable(tableName).putString(key, "value2");
    waitForNtcoreEvents();
    assertEquals("value2", source.getData().get(key));
  }

  @Test
  public void testTypeDetectedCorrectly() {
    CompositeNetworkTableSource<?> source
        = new CompositeNetworkTableSource<>(tableName, DataTypes.SendableChooser);

    NetworkTable.getTable(tableName).putString(".type", "SendableChooser");
    waitForNtcoreEvents();
    assertTrue(source.isActive());
  }

}
