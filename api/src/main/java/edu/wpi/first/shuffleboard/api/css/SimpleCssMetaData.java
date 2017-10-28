package edu.wpi.first.shuffleboard.api.css;

import edu.wpi.first.shuffleboard.api.properties.SimpleStyleableObjectPropertyWrapper;

import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;

/**
 * A simple implementation of CSS metadata that allows the property to be styled as long as it is not bound.
 *
 * @param <S> the type of the styleable
 * @param <T> the type of the property to be styled
 */
public class SimpleCssMetaData<S extends Styleable, T> extends CssMetaData<S, T> {

  private final Function<? super S, Property<T>> propertyExtractor;

  /**
   * Creates a new simple CSS metadata object.
   *
   * @param property          the CSS property descriptor, eg {@code "-fx-background-color"}
   * @param converter         the style converter
   * @param propertyExtractor the function to use to get the property this metadata should describe
   */
  public SimpleCssMetaData(String property,
                           StyleConverter<?, T> converter,
                           Function<? super S, Property<T>> propertyExtractor) {
    super(property, converter);
    this.propertyExtractor = propertyExtractor;
  }

  @Override
  public boolean isSettable(S styleable) {
    return !propertyExtractor.apply(styleable).isBound();
  }

  @Override
  public StyleableProperty<T> getStyleableProperty(S styleable) {
    Property<T> property = propertyExtractor.apply(styleable);
    if (property instanceof StyleableProperty) {
      // no need to wrap an already styleable property
      return (StyleableProperty<T>) property;
    } else {
      return new SimpleStyleableObjectPropertyWrapper<>(this, property);
    }
  }

}
