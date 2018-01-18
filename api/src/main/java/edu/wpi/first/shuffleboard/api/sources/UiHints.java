package edu.wpi.first.shuffleboard.api.sources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a source type with hints for the UI.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UiHints {

  /**
   * Flags the source type to enable the display of a connection status indicator in the UI.
   *
   * <p>Default: {@code true}</p>
   */
  boolean showConnectionIndicator() default true;

}
