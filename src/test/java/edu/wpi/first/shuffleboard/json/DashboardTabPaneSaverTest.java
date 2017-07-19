package edu.wpi.first.shuffleboard.json;

import edu.wpi.first.shuffleboard.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.util.AsyncUtils;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;

public class DashboardTabPaneSaverTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
  }

  @Before
  public void setUp() {
    NetworkTableUtils.shutdown();
    NetworkTablesJNI.setUpdateRate(0.01);
    AsyncUtils.setAsyncRunner(Runnable::run);
    NetworkTablesJNI.putDouble("/LiveWindow/TestSystem/Ultrasonic/Value", 0.5);
    NetworkTablesJNI.putDouble("/LiveWindow/TestSystem/Compass/Value", 0.5);
    NetworkTablesJNI.putDouble("/LiveWindow/Elevator/p", 0.5);
    NetworkTablesJNI.putDouble("/LiveWindow/Elevator/d", 0.5);
    NetworkTablesJNI.putDouble("/LiveWindow/Elevator/f", 0.5);

    NetworkTablesJNI.putBoolean("/LiveWindow/~STATUS~/LW Enabled", true);
    NetworkTablesJNI.putBoolean("/LiveWindow/Elevator/enabled", false);
    NetworkTableUtils.waitForNtcoreEvents();
  }

  @After
  public void tearDown() {
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
    NetworkTableUtils.shutdown();
  }

  @Test
  public void testDeserialize() throws Exception {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream("/smartdashboard.json"), "UTF-8");
    DashboardTabPane dashboard = JsonBuilder.forSaveFile().fromJson(reader, DashboardTabPane.class);

    assertEquals(2 + 1, dashboard.getTabs().size()); // 1 for the adder tab

    DashboardTabPane.DashboardTab firstTab = (DashboardTabPane.DashboardTab) dashboard.getTabs().get(0);
    assertEquals("First Tab", firstTab.getTitle());
    assertEquals(6, firstTab.getWidgetPane().getTiles().size());
  }
}