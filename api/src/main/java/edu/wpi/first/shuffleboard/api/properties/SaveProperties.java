package edu.wpi.first.shuffleboard.api.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Do not use this annotation directly - use {@link SavePropertyFrom} instead. This annotation only exists so that
 * {@code SavePropertyFrom} can be repeated.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveProperties {

  /**
   * Do not use.
   */
  SavePropertyFrom[] value();

}
