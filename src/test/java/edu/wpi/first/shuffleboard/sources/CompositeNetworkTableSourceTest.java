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
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
    waitForNtcoreListeners();
    assertEquals("value1", source.getData().get(key));

    NetworkTable.getTable(tableName).putString(key, "value2");
    waitForNtcoreListeners();
    assertEquals("value2", source.getData().get(key));
  }

  @Test
  public void testTypeDetectedCorrectly() {
    CompositeNetworkTableSource source
        = new CompositeNetworkTableSource(tableName, DataType.RobotDrive);

    NetworkTable.getTable(tableName).putString(".metadata/Type", "RobotDrive");
    waitForNtcoreListeners();
    assertTrue(source.isActive());
  }

  private static void waitForNtcoreListeners() {
    WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
  }

}
