package edu.wpi.first.shuffleboard.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a plugin class as requiring other plugins. This used so that the requirements exist on the class-level,
 * allowing plugins to reference classes or types not present on the Shuffleboard classpath at load time.
 *
 * <p>Rather than using this annotation directly, the {@link Requires @Requires} annotation can be used multiple
 * times on a single class, reducing clutter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Requirements {

  /**
   * The dependencies of the plugin.
   */
  Requires[] value();

}
