package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.Widgets;

import org.junit.jupiter.api.AfterAll;
import org.testfx.framework.junit5.ApplicationTest;

public abstract class AbstractWidgetTest extends ApplicationTest {

  protected static void setRequirements(Class<? extends Widget> widgetClass, DataType... dataTypes) {
    Widgets widgetRegistry = new Widgets();
    DataTypes dataTypeRegistry = new DataTypes();

    widgetRegistry.register(widgetClass);
    for (DataType dataType : dataTypes) {
      dataTypeRegistry.register(dataType);
    }

    Widgets.setDefault(widgetRegistry);
    DataTypes.setDefault(dataTypeRegistry);
  }

  @AfterAll
  public static void resetRegistries() {
    Widgets.setDefault(new Widgets());
    DataTypes.setDefault(new DataTypes());
  }

}
