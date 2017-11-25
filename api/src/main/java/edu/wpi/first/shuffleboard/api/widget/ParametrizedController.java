package edu.wpi.first.shuffleboard.api.widget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for FXML-based widgets that contains the path to the FXML file.
 * Widgets with this annotation are the FXML controller class for that FXML file.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParametrizedController {

  /**
   * The path to the FXML file for the widget with this annotation. This can be either an <i>absolute</i> path or a
   * <i>relative</i> path to the class on which the annotation is placed.
   */
  String value();

}
