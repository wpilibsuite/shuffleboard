package edu.wpi.first.shuffleboard.api.widget;

import java.util.function.Supplier;

public interface ComponentType extends Supplier<Component> {
  /**
   * Get the name of the componenet (ex: "Number Slider").
   */
  String getName();
}
