package edu.wpi.first.shuffleboard.api.tab.model;

import java.util.Map;

/**
 * A parent contains child components.
 */
public interface ParentModel {

  /**
   * Gets the layout with the given path and type, creating if if it does not already exist.
   *
   * @param path       the path to the layout
   * @param layoutType the type of the layout eg "List", "Grid"
   *
   * @return the layout
   */
  LayoutModel getLayout(String path, String layoutType);

  /**
   * Adds a child to this parent. Does nothing if the component is already a child.
   *
   * @param component the child component to add
   */
  void addChild(ComponentModel component);

  /**
   * Gets the child in this parent with the given path.
   *
   * @param path the full path to the child eg "/Shuffleboard/Tab/Layout1/Layout2/.../LayoutN/Child"
   *
   * @return the child, or null if no such child exists in this parent
   */
  ComponentModel getChild(String path);

  /**
   * Gets the child with the given path, creating it if it does not already exist. The existing child, if present,
   * will have its display type and properties updated.
   *
   * @param path        the full path to th child
   * @param type        the type of the child to create. Only used if no child exists with the given path
   * @param displayType the display type of the child
   * @param properties  the properties of the child
   *
   * @return the child
   */
  default ComponentModel getOrCreate(String path, String type, String displayType, Map<String, Object> properties) {
    if (getChild(path) == null) {
      ComponentModelImpl component = new ComponentModelImpl(path, this, type, displayType, properties);
      addChild(component);
    } else {
      ComponentModel child = getChild(path);
      child.setDisplayType(displayType);
      child.setProperties(properties);
    }
    return getChild(path);
  }

  /**
   * Gets the children of this parent. Children are mapped to their paths.
   */
  Map<String, ComponentModel> getChildren();
}
