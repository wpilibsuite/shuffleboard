package edu.wpi.first.shuffleboard.app.sources;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.MapData;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static edu.wpi.first.shuffleboard.api.util.NetworkTableUtils.waitForNtcoreEvents;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompositeNetworkTableSourceTest {

  private static final String tableName = "CompositeNetworkTableSourceTest";

  @BeforeEach
  public void setUp() {
    NetworkTableUtils.shutdown();
    AsyncUtils.setAsyncRunner(Runnable::run);
  }

  @AfterEach
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
