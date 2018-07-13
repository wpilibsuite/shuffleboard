package edu.wpi.first.shuffleboard.api.tab.model;

import java.util.Map;

/**
 * Something that has arbitrary properties.
 */
public interface PropertyHolder {

  /**
   * Gets the properties.
   */
  Map<String, Object> getProperties();

  /**
   * Sets the properties to the given ones. Existing properties not in the given map will be removed.
   */
  void setProperties(Map<String, Object> properties);
}
