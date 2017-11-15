package edu.wpi.first.shuffleboard.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a dependency of a plugin.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Dependencies.class)
public @interface Dependency {

  /**
   * The group ID of the plugin being depended on.
   */
  String group();

  /**
   * The name of the plugin being depended on.
   */
  String name();

  /**
   * The <i>minimum</i> version of the plugin that can be depended on.
   */
  String minVersion();
}
