package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.data.DataType;

import java.util.Set;
import java.util.function.Supplier;

public interface WidgetType extends Supplier<Widget> {

  /**
   * Get the name of the widget (ex: "Number Slider").
   */
  String getName();

  /**
   * Get data types the widget should be suggested for.
   */
  Set<DataType> getDataTypes();
}
