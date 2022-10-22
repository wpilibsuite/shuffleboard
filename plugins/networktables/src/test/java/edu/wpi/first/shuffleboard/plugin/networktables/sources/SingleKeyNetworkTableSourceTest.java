package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SingleKeyNetworkTableSourceTest {

  private NetworkTable table;
  private NetworkTableInstance inst;

  @BeforeEach
  public void setUp() {
    AsyncUtils.setAsyncRunner(Runnable::run);
    inst = NetworkTableInstance.create();
    NetworkTableSourceType.setInstance(new NetworkTableSourceType(new NetworkTablesPlugin(inst)));
    table = inst.getTable("");
  }

  @AfterEach
  public void tearDown() {
    inst.close();
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
  }

  @Test
  public void testInactiveByDefault() {
    String key = "key";
    DataType type = DataTypes.All;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    assertFalse(source.isActive(), "The source should not be active without any data");
    assertEquals(type.getDefaultValue(), source.getData(), "The source should not have any data");
    source.close();
  }

  @Test
  @Disabled("Race conditions in dependencies")
  public void testValueUpdates() throws TimeoutException {
    String key = "key";
    DataType type = DataTypes.All;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    table.getEntry(key).setString("a value");
    NetworkTableInstance.getDefault().waitForListenerQueue(1.0);
    assertEquals("a value", source.getData());
    assertTrue(source.isActive(), "The source should be active");
    source.close();
  }

  @Test
  public void testWrongDataType() throws TimeoutException {
    String key = "key";
    DataType type = DataTypes.All;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    table.getEntry(key).setNumber(12345);
    NetworkTableInstance.getDefault().waitForListenerQueue(1.0);
    assertEquals(type.getDefaultValue(), source.getData(), "The source should not have any data");
    assertFalse(source.isActive());
    source.close();
  }
}
