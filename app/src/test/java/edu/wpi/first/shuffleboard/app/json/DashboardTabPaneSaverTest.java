package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.InputStreamReader;
import java.io.Reader;

import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class DashboardTabPaneSaverTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    // Just here so we can run on the FX thread
  }

  @BeforeEach
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

  @AfterEach
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
