package edu.wpi.first.shuffleboard.api.widget;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A helper class for creating ComponentTypes from Layout classes and a name.
 */
public class LayoutClass<T extends Layout> implements ComponentType {

  private final String name;
  private final Class<T> layoutClass;

  public LayoutClass(String name, Class<T> layout) {
    this.name = name;
    this.layoutClass = layout;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Component get() {
    return Widgets.viewFor(layoutClass).orElseGet(() -> {
      try {
        return layoutClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        Logger.getLogger("Widgets").log(Level.WARNING, "error creating widget", e);
        return null;
      }
    });
  }
}
