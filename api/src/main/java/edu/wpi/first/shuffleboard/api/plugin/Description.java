package edu.wpi.first.shuffleboard.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a plugin.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

  /**
   * The group ID of the plugin.
   */
  String group();

  /**
   * The name of the plugin.
   */
  String name();

  /**
   * The current version of the plugin. This <i>must</i> follow
   * <a href="http://semver.org">semantic versioning</a> guidelines.
   */
  String version();

  /**
   * A summary of the plugin. This should state the widgets and sources provided by the plugin.
   */
  String summary();

}
