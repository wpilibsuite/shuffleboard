package edu.wpi.first.shuffleboard.plugin.networktables.sources;


import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SingleKeyNetworkTableSourceTest {

  private ITable table;

  @BeforeEach
  public void setUp() {
    NetworkTableUtils.shutdown();
    NetworkTablesJNI.setUpdateRate(0.01);
    AsyncUtils.setAsyncRunner(Runnable::run);
    table = NetworkTable.getTable("");
  }

  @AfterEach
  public void tearDown() {
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
    NetworkTableUtils.shutdown();
  }

  @Test
  public void testInactiveByDefault() {
    String key = "key";
    DataType type = DataTypes.All;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    assertFalse(source.isActive(), "The source should not be active without any data");
    assertNull(source.getData(), "The source should not have any data");
  }

  @Test
  @Disabled("Race conditions in dependencies")
  public void testValueUpdates() throws TimeoutException {
    String key = "key";
    DataType type = DataTypes.All;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    table.putString(key, "a value");
    NetworkTableUtils.waitForNtcoreEvents();
    assertEquals("a value", source.getData());
    assertTrue(source.isActive(), "The source should be active");
  }

  @Test
  public void testWrongDataType() throws TimeoutException {
    String key = "key";
    DataType type = DataTypes.All;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    table.putNumber(key, 12345);
    assertEquals(null, source.getData(), "The source should not have any data");
    assertFalse(source.isActive());
  }

}
