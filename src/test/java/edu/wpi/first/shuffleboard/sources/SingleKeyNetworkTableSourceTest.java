package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.NetworkTableRequired;
import edu.wpi.first.shuffleboard.util.AsyncUtils;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class SingleKeyNetworkTableSourceTest {

  private ITable table;

  @Before
  public void setUp() {
    NetworkTableRequired.setUpNetworkTables();
    AsyncUtils.setAsyncRunner(Runnable::run);
    table = NetworkTable.getTable("");
  }

  @After
  public void tearDown() {
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
    NetworkTableRequired.tearDownNetworkTables();
  }

  @Test
  public void testInactiveByDefault() {
    String key = "key";
    DataType type = DataType.String;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    assertFalse("The source should not be active without any data", source.isActive());
    assertNull("The source should not have any data", source.getData());
  }

  @Test
  public void testValueUpdates() throws TimeoutException {
    String key = "key";
    DataType type = DataType.String;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    table.putString(key, "a value");
    // wait for network table listeners to fire
    WaitForAsyncUtils.waitFor(100, TimeUnit.MILLISECONDS, source::isActive);
    assertEquals("a value", source.getData());
    assertTrue("The source should be active", source.isActive());
  }

  @Test
  public void testWrongDataType() throws TimeoutException {
    String key = "key";
    DataType type = DataType.String;
    SingleKeyNetworkTableSource<String> source
        = new SingleKeyNetworkTableSource<>(table, key, type);
    table.putNumber(key, 12345);
    assertEquals("The source should not have any data", null, source.getData());
    assertFalse(source.isActive());
  }

}
