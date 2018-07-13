package edu.wpi.first.shuffleboard.api.tab.model;

/**
 * Model for a single tab in Shuffleboard.
 */
public interface TabModel extends ParentModel, PropertyHolder {
  /**
   * Gets the title of this tab.
   */
  String getTitle();
}
