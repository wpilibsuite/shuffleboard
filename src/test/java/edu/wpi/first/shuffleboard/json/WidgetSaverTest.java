package edu.wpi.first.shuffleboard.json;

import edu.wpi.first.shuffleboard.sources.SourceType;
import edu.wpi.first.shuffleboard.util.AsyncUtils;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.assertEquals;

public class WidgetSaverTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
  }

  @Before
  public void setUp() {
    NetworkTableUtils.shutdown();
    NetworkTablesJNI.setUpdateRate(0.01);
    AsyncUtils.setAsyncRunner(Runnable::run);
    NetworkTablesJNI.putDouble("/value", 0.5);
    NetworkTableUtils.waitForNtcoreEvents();
  }

  @After
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
    assertEquals(SourceType.NETWORK_TABLE, widget.getSource().getType());
    assertEquals(0.5, widget.getSource().getData());

    assertEquals(-1.0, getPropertyValue(widget, "min"));
    assertEquals(1.0, getPropertyValue(widget, "max"));
    assertEquals(0.0625, getPropertyValue(widget, "blockIncrement"));
  }
}