package edu.wpi.first.shuffleboard.api.tab.model;

/**
 * Model for a single tab in Shuffleboard.
 */
public interface TabModel extends ParentModel, PropertyHolder, Titled {
  /**
   * Gets the title of this tab.
   */
  @Override
  String getTitle();
}
