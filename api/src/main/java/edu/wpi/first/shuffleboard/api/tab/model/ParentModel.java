package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import java.util.Map;
import java.util.function.Supplier;

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
   * Gets the child widget with the given path, creating it if it does not already exist. The existing widget,
   * if present, will have its display type and properties updated.
   *
   * @param path        the full path to the child widget
   * @param displayType the display type of the widget
   * @param properties  the properties of the widget
   *
   * @return the widget
   *
   * @throws IllegalArgumentException if the component specified by the given path already exists and is not a widget
   */
  default WidgetModel getOrCreate(String path,
                                  Supplier<? extends DataSource<?>> sourceSupplier,
                                  String displayType,
                                  Map<String, Object> properties) {
    ComponentModel existingChild = getChild(path);
    if (existingChild == null) {
      WidgetModel widget = new WidgetModelImpl(path, this, sourceSupplier, displayType, properties);
      addChild(widget);
      return widget;
    } else {
      if (!(existingChild instanceof WidgetModel)) {
        throw new IllegalArgumentException("The child specified by the path '" + path + "' is not a widget");
      }
      existingChild.setDisplayType(displayType);
      existingChild.setProperties(properties);
      return (WidgetModel) existingChild;
    }
  }

  /**
   * Gets the children of this parent. Children are mapped to their paths.
   */
  Map<String, ComponentModel> getChildren();
}
