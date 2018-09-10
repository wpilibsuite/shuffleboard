package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.types.AllType;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.properties.SaveThisProperty;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("UI")
public class WidgetSaverTest extends ApplicationTest {

  @Description(name = "Simple Widget", dataTypes = AllType.class)
  public static class SimpleWidget extends SimpleAnnotatedWidget {

    private final SimpleDoubleProperty min = new SimpleDoubleProperty(this, "min", 0);
    private final SimpleDoubleProperty max = new SimpleDoubleProperty(this, "max", 0);
    private final SimpleDoubleProperty blockIncrement = new SimpleDoubleProperty(this, "blockIncrement", 0);

    @Override
    public List<Group> getSettings() {
      return ImmutableList.of(
          Group.of("Test",
              Setting.of("min", min),
              Setting.of("max", max),
              Setting.of("blockIncrement", blockIncrement)
          )
      );
    }

    @Override
    public Pane getView() {
      return new Pane();
    }

  }

  @Description(name = "WidgetWithSavedProperties", dataTypes = AllType.class)
  public static class WidgetWithSavedProperties extends SimpleAnnotatedWidget {

    @SaveThisProperty
    final DoubleProperty property = new SimpleDoubleProperty(this, "property", 0);
    @SaveThisProperty(name = "mathematical constant")
    final DoubleProperty property2 = new SimpleDoubleProperty(this, "property2", 0);

    @Override
    public Pane getView() {
      return new Pane();
    }
  }

  @Description(name = "WidgetSavingPropertiesFromFields", dataTypes = AllType.class)
  public static class WidgetSavingPropertiesFromFields extends SimpleAnnotatedWidget {

    @SavePropertyFrom(propertyName = "text", savedName = "example text")
    final TextField textField = new TextField("text");

    @SavePropertyFrom(propertyName = "value", savedName = "a property")
    @SavePropertyFrom(propertyName = "min", savedName = "minimum")
    @SavePropertyFrom(propertyName = "max", savedName = "maximum")
    @SavePropertyFrom(propertyName = "visible", savedName = "showSlider")
    final Slider slider = new Slider(0, 100, 0);

    @Override
    public Pane getView() {
      return new Pane();
    }
  }

  @Override
  public void start(Stage stage) throws Exception {
    // Just here so can run on the FX thread
  }

  @BeforeEach
  public void setUp() {
    Components.setDefault(new Components());
    DataTypes.setDefault(new DataTypes());
  }

  @AfterEach
  public void tearDown() {
    Components.setDefault(new Components());
    DataTypes.setDefault(new DataTypes());
  }

  private static Object getPropertyValue(Widget widget, String name) {
    return widget.getSettings().get(0)
        .getSettings()
        .stream()
        .filter(p -> p.getName().equals(name))
        .findFirst()
        .orElseThrow(RuntimeException::new)
        .getProperty()
        .getValue();
  }

  @Test
  @Tag("NonJenkinsTest") // More info here: https://github.com/wpilibsuite/shuffleboard/issues/214
  public void loadSimpleWidget() throws Exception {
    Components.getDefault().register(SimpleWidget.class);
    String widgetJson = "{\n"
        + "\"_type\": \"Simple Widget\",\n"
        + "\"_source0\": \"example://All\",\n"
        + "\"Test/min\": -1.0,\n"
        + "\"Test/max\": 1.0,\n"
        + "\"Test/blockIncrement\": 0.0625\n"
        + "}";

    Widget widget = JsonBuilder.forSaveFile().fromJson(widgetJson, Widget.class);

    assertEquals("Simple Widget", widget.getName());
    assertEquals(SourceTypes.Static, widget.getSources().get(0).getType());

    assertEquals(-1.0, getPropertyValue(widget, "min"));
    assertEquals(1.0, getPropertyValue(widget, "max"));
    assertEquals(0.0625, getPropertyValue(widget, "blockIncrement"));
  }

  @Test
  public void widgetWithSavedProperties() {
    // given
    Components.getDefault().register(WidgetWithSavedProperties.class);
    final Gson gson = JsonBuilder.forSaveFile();
    final WidgetWithSavedProperties saveMe = new WidgetWithSavedProperties();
    final double property1Value = 555.555;
    final double property2Value = Math.PI;
    saveMe.property.set(property1Value);
    saveMe.property2.set(property2Value);

    // Test saving
    JsonObject jsonObject = gson.toJsonTree(saveMe, Widget.class).getAsJsonObject();

    assertAll(
        () -> assertEquals(property1Value, jsonObject.get("property").getAsDouble(), "property value not saved"),
        () -> assertEquals(property2Value, jsonObject.get("mathematical constant").getAsDouble(),
            "property2 value not saved")
    );

    // Test loading
    Widget read = gson.fromJson(jsonObject, Widget.class);
    assertEquals(WidgetWithSavedProperties.class, read.getClass());

    WidgetWithSavedProperties actualRead = (WidgetWithSavedProperties) read;

    assertAll(
        () -> assertEquals(property1Value, actualRead.property.get(), "property value not read"),
        () -> assertEquals(property2Value, actualRead.property2.get(), "property2 value not read")
    );
  }

  @Test
  public void widgetWithSavedFields() {
    Components.getDefault().register(WidgetSavingPropertiesFromFields.class);
    final Gson gson = JsonBuilder.forSaveFile();
    final WidgetSavingPropertiesFromFields saveMe = new WidgetSavingPropertiesFromFields();
    saveMe.textField.setText("new text");
    saveMe.slider.setValue(75);
    saveMe.slider.setVisible(false);

    // Test saving
    JsonObject jsonObject = gson.toJsonTree(saveMe, Widget.class).getAsJsonObject();

    assertAll(
        () -> assertEquals("new text", jsonObject.get("example text").getAsString()),
        () -> assertEquals(0, jsonObject.get("minimum").getAsDouble()),
        () -> assertEquals(75, jsonObject.get("a property").getAsDouble()),
        () -> assertEquals(100, jsonObject.get("maximum").getAsDouble()),
        () -> assertFalse(jsonObject.get("showSlider").getAsBoolean())
    );

    // Test loading
    Widget read = gson.fromJson(jsonObject, Widget.class);
    assertEquals(WidgetSavingPropertiesFromFields.class, read.getClass());

    WidgetSavingPropertiesFromFields actualRead = (WidgetSavingPropertiesFromFields) read;

    assertAll(
        () -> assertEquals("new text", actualRead.textField.getText()),
        () -> assertEquals(0, actualRead.slider.getMin()),
        () -> assertEquals(75, actualRead.slider.getValue()),
        () -> assertEquals(100, actualRead.slider.getMax()),
        () -> assertFalse(actualRead.slider.isVisible())
    );
  }

}
