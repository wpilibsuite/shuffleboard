package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.Components;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.testfx.framework.junit5.ApplicationTest;

@Tag("UI")
public abstract class AbstractWidgetTest extends ApplicationTest {

  protected static void setRequirements(Class<? extends Widget> widgetClass, DataType... dataTypes) {
    Components widgetRegistry = new Components();
    DataTypes dataTypeRegistry = new DataTypes();

    widgetRegistry.register(widgetClass);
    for (DataType dataType : dataTypes) {
      dataTypeRegistry.registerIfAbsent(dataType);
    }

    Components.setDefault(widgetRegistry);
    DataTypes.setDefault(dataTypeRegistry);
  }

  @AfterAll
  public static void resetRegistries() {
    Components.setDefault(new Components());
    DataTypes.setDefault(new DataTypes());
  }

}
