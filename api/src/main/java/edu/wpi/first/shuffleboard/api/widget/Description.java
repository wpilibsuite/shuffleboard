package edu.wpi.first.shuffleboard.api.widget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a widget.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

  /**
   * The name of the widget. This must be unique among all widgets. If a widget is already
   * registered with the same name, an exception will be thrown when attempting to register
   * the widget.
   */
  String name();

  /**
   * A short summary of the widget that states what kind of values it can display and how to use it.
   */
  String summary() default "";

  /**
   * The types for the sources the widget can handle.
   */
  Class<?>[] dataTypes();

}
