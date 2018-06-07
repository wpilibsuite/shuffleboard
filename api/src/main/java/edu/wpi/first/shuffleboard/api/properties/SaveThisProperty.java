package edu.wpi.first.shuffleboard.api.properties;

import edu.wpi.first.shuffleboard.api.widget.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JavaFX property field in a component to be saved. Properties that are made available via
 * {@link Component#getSettings()} will be saved and loaded without needing this annotation; this annotation should only
 * be placed on properties that widget authors do not want to be made user-configurable through the properties editor,
 * or when the name of the property is not particularly descriptive.
 *
 * <p>For example, this will save a property named {@code foo} with the name of the property ("foo"):
 * <pre>{@code
 *@literal @SaveThisProperty
 * private final Property<Foo> foo = new SimpleObjectProperty(this, "foo", new Foo());
 * }</pre>
 * This will save it as "a foo":
 * <pre>{@code
 *@literal @SaveThisProperty(name = "a foo")
 * private final Property<Foo> foo = new SimpleObjectProperty(this, "foo", new Foo());
 * }</pre>
 *
 * <p>If the property has no name (i.e. the name string is {@code null} or {@code ""}), then the annotation <i>must</i>
 * set the name. Otherwise, an exception will be thrown when attempting to save or load the widget.
 * <pre>{@code
 * // No name set!
 *@literal @SaveThisProperty
 * private final Property<Foo> foo = new SimpleObjectProperty(this, "", new Foo());
 * }</pre>
 * <pre>{@code
 * // Good - the name is set in the annotation
 *@literal @SaveThisProperty(name = "a foo")
 * private final Property<Foo> foo = new SimpleObjectProperty(this, "", new Foo());
 * }</pre>
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
