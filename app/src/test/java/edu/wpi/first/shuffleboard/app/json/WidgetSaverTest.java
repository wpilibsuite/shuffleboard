package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.types.AllType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.widget.AnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.Widgets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WidgetSaverTest extends ApplicationTest {

  @Description(name = "Simple Widget", dataTypes = AllType.class)
  public static class SimpleWidget extends AnnotatedWidget {

    public SimpleWidget() {
      exportProperties(
          new SimpleDoubleProperty(this, "min", 0),
          new SimpleDoubleProperty(this, "max", 0),
          new SimpleDoubleProperty(this, "blockIncrement", 0)
      );
    }

    @Override
    public Pane getView() {
      return null;
    }

  }

  @Override
  public void start(Stage stage) throws Exception {
    // Just here so can run on the FX thread
  }

  @BeforeEach
  public void setUp() {
    Widgets.setDefault(new Widgets());
    DataTypes.setDefault(new DataTypes());
  }

  @AfterEach
  public void tearDown() {
    Widgets.setDefault(new Widgets());
    DataTypes.setDefault(new DataTypes());
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
    Widgets.getDefault().register(SimpleWidget.class);
    String widgetJson = "{\n"
        + "\"_type\": \"Simple Widget\",\n"
        + "\"_source\": \"example://All\",\n"
        + "\"min\": -1.0,\n"
        + "\"max\": 1.0,\n"
        + "\"blockIncrement\": 0.0625\n"
        + "}";

    Widget widget = JsonBuilder.forSaveFile().fromJson(widgetJson, Widget.class);

    assertEquals("Simple Widget", widget.getName());
    assertEquals(SourceTypes.Static, widget.getSource().getType());

    assertEquals(-1.0, getPropertyValue(widget, "min"));
    assertEquals(1.0, getPropertyValue(widget, "max"));
    assertEquals(0.0625, getPropertyValue(widget, "blockIncrement"));
  }
}
