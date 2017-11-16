package edu.wpi.first.shuffleboard.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a requirement of a plugin. This allows plugins to declare their dependency on another plugin to provide
 * data types, sources, widgets, or an API that they depend on. A plugin with a dependency will not be allowed to be
 * loaded if there is no loaded plugin with the same group ID, name, and compatible version (for version compatibility
 * details, see {@link #minVersion()}).
 *
 * <p>This prevents {@link NoClassDefFoundError NoClassDefFoundErrors} or {@link NoSuchMethodError NoSuchMethodErrors}
 * being thrown when loading or instantiating a plugin class when a JAR containing a plugin it depends on is not present
 * or on the classpath.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Requirements.class)
public @interface Requires {

  /**
   * The group ID of the plugin being depended on.
   */
  String group();

  /**
   * The name of the plugin being depended on.
   */
  String name();

  /**
   * The <i>minimum</i> version of the plugin that can be depended on. A loaded plugin with the same major version
   * number, and at least the same minor version number, will allow this plugin to be loaded. If no plugin is loaded
   * with a compatible version, this plugin will not be allowed to be loaded.
   *
   * <p>For example, if the minimum compatible version is {@code "1.2.3"} and a plugin is present with version
   * {@code "1.4.6"}, this plugin will be allowed to load since there is a compatible, albeit more recent, version of
   * the dependency. However, if only version {@code "1.1.1"} is loaded, this plugin cannot be loaded because the loaded
   * version of the dependency is too old. The same goes if a version {@code "2.2.3"} is loaded; it is too recent and
   * does not have a backward-compatible API with {@code "1.2.3"}.
   */
  String minVersion();
}
