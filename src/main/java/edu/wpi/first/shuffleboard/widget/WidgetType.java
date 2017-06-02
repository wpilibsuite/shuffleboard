package edu.wpi.first.shuffleboard.widget;

import java.util.Set;
import java.util.function.Supplier;

public interface WidgetType extends Supplier<Widget> {
  /**
   * @return The name of the widget.
   */
  String getName();

  /**
   * @return The data types the widget should be suggested for.
   */
  Set<DataType> getDataTypes();
}
