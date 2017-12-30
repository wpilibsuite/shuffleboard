package edu.wpi.first.shuffleboard.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a plugins group ID (a unique identifier for the group or organization that develops the plugin); and its
 * name, version, and a summary of what the plugin provides.
 *
 * <p><strong>This annotation <i>must</i> be present on a plugin class, or it will not be able to be loaded.</strong>
 *
 * <p>Note that the version <i>must</i> follow <a href="http://semver.org">semantic versioning</a> guidelines.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

  /**
   * The group ID of the plugin. This cannot contain a colon ({@code ':'}) character.
   */
  String group();

  /**
   * The name of the plugin. This cannot contain a colon ({@code ':'}) character.
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
