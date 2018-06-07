package edu.wpi.first.shuffleboard.api.properties;

import edu.wpi.first.shuffleboard.api.widget.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flags a property of a field to be saved as part of that object.
 *
 * <p>For example, take a widget class has a slider that controls the speed of something. In the old API, the widget
 * would have export its properties directly (which doesn't give descriptive names in the widget's context, and can
 * run into conflicts), or create new properties with the desired names and bind those to the properties of the slider.
 * This takes a lot of code and makes the class less readable; there is a lot of boilerplate. It is also not ideal,
 * because properties exported via {@link Component#getSettings()} are also configurable through the property editor for
 * the widget, even if the author does not want them to be user-editable, or if they are already editable through the
 * controls in the widget; for example, the value of the slider changes when a user moves the slider - it does not need
 * to be editable another way.
 *
 * <pre>{@code
 * // Bad API -- exposes properties to the user that should not be editable!
 * class MyWidget implements Widget {
 *
 *   private final Slider speedSlider = new Slider();
 *
 *   private final DoubleProperty speed = new SimpleDoubleProperty(this, "speed");
 *   private final DoubleProperty minSpeed = new SimpleDoubleProperty(this, "minSpeed");
 *   private final DoubleProperty maxSpeed = new SimpleDoubleProperty(this, "maxSpeed");
 *
 *   public MyWidget() {
 *     speedSlider.valueProperty().bindBidirectional(speed);
 *     speedSlider.minProperty().bindBidirectional(minSpeed);
 *     speedSlider.maxProperty().bindBidirectional(maxSpeed);
 *   }
 *
 *  @literal @Override
 *   public List<Property> getProperties() {
 *     return ImmutableList.of(
 *       speed,
 *       minSpeed,
 *       maxSpeed
 *     );
 *   }
 * }
 * }</pre>
 *
 * <p>A better API is to use this annotation to specify the properties to save and the names to save them as. No
 * properties have to be exposed to users, no dummy properties have to be created to set the name, and the code is
 * <i>much</i> clearer:
 *
 * <pre>{@code
 * class MyWidget implements Widget {
 *
 *  @literal @SavePropertyFrom(propertyName = "value", savedName = "speed")
 *  @literal @SavePropertyFrom(propertyName = "min", savedName = "minSpeed")
 *  @literal @SavePropertyFrom(propertyName = "max", savedName = "maxSpeed")
 *   private final Slider speedSlider = new Slider();
 *
 * }
 * }</pre>
 *
 * <h2>Using</h2>
 * An empty string for either {@code propertyName} or {@code savedName} will throw an exception when the widget is
 * attempted to be saved or loaded: {@code @SavePropertyFrom(propertyName="", savedName="")} is not allowed.
 * <br>
 * The property must have public "getter" and "setter" methods; for example, a property with name "foo" must have
 * {@code public Foo getFoo()} and {@code public void setFoo(Foo newFoo)} methods (boolean properties may also use
 * the prefix {@code is} instead of {@code get}).
 * <br>
 * Note that this only uses getter and setter methods, so this annotation is fully compatible with standard Java beans.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(SaveProperties.class)
public @interface SavePropertyFrom {

  /**
   * The name of the property to save.
   */
  String propertyName();

  /**
   * The name to save the property as. By default, the name of the property is used, but can be overridden by setting
   * this value.
   */
  String savedName() default "";

}
