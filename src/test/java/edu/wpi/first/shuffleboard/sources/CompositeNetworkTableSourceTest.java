package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.NetworkTableRequired;
import edu.wpi.first.shuffleboard.util.AsyncUtils;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import javafx.collections.FXCollections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static edu.wpi.first.shuffleboard.util.NetworkTableUtils.waitForNtcoreEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompositeNetworkTableSourceTest {

  private static final String tableName = "CompositeNetworkTableSourceTest";

  @Before
  public void setUp() {
    NetworkTableRequired.setUpNetworkTables();
    AsyncUtils.setAsyncRunner(Runnable::run);
  }

  @After
  public void tearDown() {
    NetworkTableRequired.tearDownNetworkTables();
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
  }

  @Test
  public void testInactiveByDefault() {
    CompositeNetworkTableSource source = new CompositeNetworkTableSource(tableName, DataType.Map);
    assertFalse(source.isActive());
    assertTrue(source.getData().isEmpty());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCannotSetDataDirectly() {
    CompositeNetworkTableSource source = new CompositeNetworkTableSource(tableName, DataType.Map);
    source.setData(FXCollections.observableHashMap());
  }

  @Test
  public void testDataUpdates() {
    CompositeNetworkTableSource source = new CompositeNetworkTableSource(tableName, DataType.Map);
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
    CompositeNetworkTableSource source
        = new CompositeNetworkTableSource(tableName, DataType.RobotDrive);

    NetworkTable.getTable(tableName).putString(".metadata/Type", "RobotDrive");
    waitForNtcoreEvents();
    assertTrue(source.isActive());
  }

}
