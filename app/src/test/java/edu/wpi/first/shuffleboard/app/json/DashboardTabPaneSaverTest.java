package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.networktables.NetworkTableInstance;

import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.InputStreamReader;
import java.io.Reader;

import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Depends on network tables")
public class DashboardTabPaneSaverTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    // Just here so we can run on the FX thread
  }

  @BeforeEach
  public void setUp() {
    NetworkTableUtils.shutdown();
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.setUpdateRate(0.01);
    AsyncUtils.setAsyncRunner(Runnable::run);
    inst.getEntry("/LiveWindow/TestSystem/Ultrasonic/Value").setDouble(0.5);
    inst.getEntry("/LiveWindow/TestSystem/Compass/Value").setDouble(0.5);
    inst.getEntry("/LiveWindow/Elevator/p").setDouble(0.5);
    inst.getEntry("/LiveWindow/Elevator/d").setDouble(0.5);
    inst.getEntry("/LiveWindow/Elevator/f").setDouble(0.5);

    inst.getEntry("/LiveWindow/~STATUS~/LW Enabled").setBoolean(true);
    inst.getEntry("/LiveWindow/Elevator/enabled").setBoolean(false);
    inst.waitForEntryListenerQueue(-1.0);
  }

  @AfterEach
  public void tearDown() {
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
    NetworkTableUtils.shutdown();
  }

  @Test
  public void testDeserialize() throws Exception {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream("/test.json"), "UTF-8");
    DashboardTabPane dashboard = JsonBuilder.forSaveFile().fromJson(reader, DashboardTabPane.class);

    assertEquals(2 + 1, dashboard.getTabs().size()); // 1 for the adder tab

    DashboardTab firstTab = (DashboardTab) dashboard.getTabs().get(0);
    assertEquals("First Tab", firstTab.getTitle());
    assertEquals(6, firstTab.getWidgetPane().getTiles().size());
  }
}
