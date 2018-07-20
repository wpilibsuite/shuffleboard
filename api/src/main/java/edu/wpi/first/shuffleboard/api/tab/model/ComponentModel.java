package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

/**
 * Represents a single component in Shuffleboard. This can be either a widget or a layout.
 */
public interface ComponentModel extends PropertyHolder, Titled {

  /**
   * Gets the path to this component. This is always constant, regardless of the actual component's title.
   */
  String getPath();

  /**
   * Gets the title of this component.
   */
  @Override
  default String getTitle() {
    return getPath().substring(getPath().lastIndexOf('/') + 1);
  }

  /**
   * Gets the parent of this component.
   */
  ParentModel getParent();

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

  /**
   * Sets the preferred position of this component. If no position is set, or if the position is already occupied,
   * Shuffleboard will determine an open position in which to place the component.
   */
  void setPreferredPosition(GridPoint point);

  /**
   * Gets the preferred position of this component, as specified by {@link #setPreferredPosition}.
   */
  GridPoint getPreferredPosition();

  /**
   * Sets the preferred size of this component. If no size is set, then Shuffleboard will use the default size of the
   * component of the specified display type. Requires {@link #setPreferredPosition the preferred position} to be set.
   */
  void setPreferredSize(TileSize size);

  /**
   * Gets the preferred size of this component, as specified by {@link #setPreferredSize}.
   */
  TileSize getPreferredSize();

}
