package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.Set;

/**
 * WidgetType extends ComponentType with Widget-specific metadata.
 */
public interface WidgetType extends ComponentType {
  /**
   * Get data types the widget should be suggested for.
   */
  Set<DataType> getDataTypes();
}
