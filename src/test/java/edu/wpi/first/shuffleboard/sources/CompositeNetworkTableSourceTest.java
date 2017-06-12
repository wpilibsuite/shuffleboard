package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.data.ComplexData;
import edu.wpi.first.shuffleboard.util.AsyncUtils;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.junit.After;
import org.junit.Before;
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
    CompositeNetworkTableSource<ComplexData> source
        = new CompositeNetworkTableSource<>(tableName, DataType.Map);
    assertFalse(source.isActive());
    assertTrue(source.getData().getMap().isEmpty());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCannotSetDataDirectly() {
    CompositeNetworkTableSource source = new CompositeNetworkTableSource<>(tableName, DataType.Map);
    source.setData(null);
  }

  @Test
  public void testDataUpdates() {
    CompositeNetworkTableSource<ComplexData> source
        = new CompositeNetworkTableSource<>(tableName, DataType.Map);
    final String key = "key1";

    NetworkTable.getTable(tableName).putString(key, "value1");
    waitForNtcoreEvents();
    assertEquals("value1", source.getData().getMap().get(key));

    NetworkTable.getTable(tableName).putString(key, "value2");
    waitForNtcoreEvents();
    assertEquals("value2", source.getData().getMap().get(key));
  }

  @Test
  public void testTypeDetectedCorrectly() {
    CompositeNetworkTableSource source
        = new CompositeNetworkTableSource(tableName, DataType.SendableChooser);

    NetworkTable.getTable(tableName).putString(".type", "SendableChooser");
    waitForNtcoreEvents();
    assertTrue(source.isActive());
  }

}
