package edu.wpi.first.shuffleboard.api.properties;

import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JavaFX property field in a component to be saved. Properties that are made available via
 * {@link Widget#getProperties()} will be saved and loaded without needing this annotation; this annotation should only
 * be placed on properties that widget authors do not want to be made user-configurable through the properties editor,
 * or when the name of the property is not particularly descriptive.
 *
 * <p>Placing this annotation on a field that does not subclass {@link javafx.beans.property.Property} will have no
 * effect.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveThisProperty {

  /**
   * The name to save the property as. The only constraint on the name is that the characters should be ASCII codes
   * for maximum compatibility.
   */
  String name() default "";

}
