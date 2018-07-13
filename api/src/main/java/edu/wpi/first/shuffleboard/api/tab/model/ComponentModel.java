package edu.wpi.first.shuffleboard.api.tab.model;

/**
 * Represents a single component in Shuffleboard. This can be either a widget or a layout.
 */
public interface ComponentModel extends PropertyHolder, Titled {

  /**
   * Gets the path to this component. This is always constant, regardless of the actual component's title.
   */
  String getPath();

  default String getTitle() {
    return getPath().substring(getPath().lastIndexOf('/') + 1);
  }

  /**
   * Gets the parent of this component.
   */
  ParentModel getParent();

  /**
   * Gets the type of this component.
   */
  String getType();

  /**
   * Gets the display type of this component.
   */
  String getDisplayType();

  /**
   * Sets the display type of this component. This can change at any time; if it does change, the existing component
   * will be removed and replaced with one of the new type in the same position, if possible. Layouts that change will
   * retain the same child components.
   */
  void setDisplayType(String displayType);

}
