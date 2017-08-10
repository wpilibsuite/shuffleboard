package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.sources.NetworkTableSourceType;
import edu.wpi.first.shuffleboard.app.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WidgetSaverTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
  }

  @BeforeEach
  public void setUp() {
    NetworkTableUtils.shutdown();
    NetworkTablesJNI.setUpdateRate(0.01);
    AsyncUtils.setAsyncRunner(Runnable::run);
    NetworkTablesJNI.putDouble("/value", 0.5);
    NetworkTableUtils.waitForNtcoreEvents();
  }

  @AfterEach
  public void tearDown() {
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
    NetworkTableUtils.shutdown();
  }

  private static Object getPropertyValue(Widget widget, String name) {
    return widget.getProperties().stream()
        .filter(p -> p.getName().equals(name))
        .findFirst()
        .orElseThrow(RuntimeException::new)
        .getValue();
  }

  @Disabled
  @Test
  public void loadSimpleWidget() throws Exception {
    String widgetJson = "{\n"
        + "\"_type\": \"Number Slider\",\n"
        + "\"_source\": \"network_table:///value\",\n"
        + "\"min\": -1.0,\n"
        + "\"max\": 1.0,\n"
        + "\"blockIncrement\": 0.0625\n"
        + "}";

    Widget widget = JsonBuilder.forSaveFile().fromJson(widgetJson, Widget.class);

    assertEquals("Number Slider", widget.getName());
    assertEquals(NetworkTableSourceType.INSTANCE, widget.getSource().getType());
    assertEquals(0.5, widget.getSource().getData());

    assertEquals(-1.0, getPropertyValue(widget, "min"));
    assertEquals(1.0, getPropertyValue(widget, "max"));
    assertEquals(0.0625, getPropertyValue(widget, "blockIncrement"));
  }
}
