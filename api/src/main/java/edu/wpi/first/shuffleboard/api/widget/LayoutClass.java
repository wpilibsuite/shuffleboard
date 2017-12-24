package edu.wpi.first.shuffleboard.api.widget;

import java.util.Optional;

/**
 * A helper class for creating ComponentTypes from Layout classes and a name.
 */
public class LayoutClass<T extends Layout> implements LayoutType {

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
  public Layout get() throws ComponentInstantiationException {
    try {
      Optional<T> type = Components.viewFor(layoutClass);
      if (type.isPresent()) {
        return type.get();
      } else {
        return layoutClass.newInstance();
      }
    } catch (InstantiationException | IllegalAccessException | RuntimeException e) {
      throw new ComponentInstantiationException(this, e);
    }
  }

  @Override
  public Class<T> getType() {
    return layoutClass;
  }

}
