package edu.wpi.first.shuffleboard.api.css;

import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.scene.paint.Color;

/**
 * A convenience subclass of {@link SimpleCssMetaData} for color properties.
 */
public class SimpleColorCssMetaData<S extends Styleable> extends SimpleCssMetaData<S, Color> {

  /**
   * Creates a new simple color CSS metadata object.
   *
   * @param property          the CSS property descriptor, eg {@code "-fx-background-color"}
   * @param propertyExtractor gets the color property this metadata should describe
   */
  public SimpleColorCssMetaData(String property, Function<? super S, Property<Color>> propertyExtractor) {
    super(property, StyleConverter.getColorConverter(), propertyExtractor);
  }

}
