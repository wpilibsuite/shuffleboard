package edu.wpi.first.shuffleboard.api.properties;

import javafx.beans.property.Property;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;

/**
 * Wraps a normal JavaFX property in a styleable wrapper property. Changes to the wrapper property will be
 * propagated to the initial property.
 *
 * @param <T> the type of the property's value
 */
public class SimpleStyleableObjectPropertyWrapper<T> extends SimpleStyleableObjectProperty<T> {

  /**
   * Creates a new styleable property that wraps another.
   *
   * @param cssMetaData the CSS metadata describing this styleable property
   * @param property    the property to wrap
   */
  public SimpleStyleableObjectPropertyWrapper(CssMetaData<? extends Styleable, T> cssMetaData, Property<T> property) {
    super(cssMetaData, property.getBean(), property.getName(), property.getValue());
    addListener((__, prev, cur) -> property.setValue(cur));
  }

}
