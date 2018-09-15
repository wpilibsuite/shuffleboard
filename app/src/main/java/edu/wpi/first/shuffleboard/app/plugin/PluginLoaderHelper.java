package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.InvalidPluginDefinitionException;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.plugin.Requirements;
import edu.wpi.first.shuffleboard.api.plugin.Requires;

import com.github.zafarkhaja.semver.Version;
import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class for {@link PluginLoader}.
 */
final class PluginLoaderHelper {

  private static final Logger log = Logger.getLogger(PluginLoaderHelper.class.getName());

  private PluginLoaderHelper() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  public static Stream<Class<?>> tryLoadClass(String name, ClassLoader classLoader) {
    try {
      return Stream.of(Class.forName(name, false, classLoader));
    } catch (ClassNotFoundException e) {
      log.log(Level.WARNING, "Could not load class for name '" + name + "' with classloader " + classLoader, e);
      return Stream.empty();
    }
  }

  /**
   * Gets the description of a plugin.
   *
   * @param pluginClass the class to get the description of
   *
   * @return the description of a plugin
   *
   * @throws InvalidPluginDefinitionException if the plugin class does not have a {@code @Description} annotation
   */
  public static Description getDescription(Class<? extends Plugin> pluginClass)
      throws InvalidPluginDefinitionException {
    if (pluginClass.isAnnotationPresent(Description.class)) {
      return pluginClass.getAnnotation(Description.class);
    } else {
      // Shouldn't happen; the plugin should have been validated earlier
      throw new InvalidPluginDefinitionException("A plugin MUST have a @Description annotation");
    }
  }

  /**
   * Gets all the <i>direct requirements</i> of a plugin by extracting that data from any present
   * {@link Requirements @Requirements} and {@link Requires @Requires} annotations on the class.
   *
   * @param pluginClass the plugin class to get the dependencies of
   *
   * @return a list of the direct plugin requirements of a plugin class
   */
  public static List<Requires> getRequirements(Class<? extends Plugin> pluginClass) {
    Requirements requirements = pluginClass.getAnnotation(Requirements.class);
    Requires[] requires = pluginClass.getAnnotationsByType(Requires.class);
    return Stream.concat(
        Stream.of(requirements)
            .filter(Objects::nonNull)
            .map(Requirements::value)
            .flatMap(Stream::of),
        Stream.of(requires)
    ).collect(Collectors.toList());
  }

  /**
   * Checks if version <tt>A</tt> is a backwards-compatible with version <tt>B</tt>; that is, something that depends on
   * version <tt>B</tt> will still function when version <tt>A</tt> is present. This assumes that the versioning scheme
   * strictly follows semantic versioning guidelines and increments the major number whenever the API has a change that
   * breaks backwards compatibility.
   *
   * @param versionA the newer version
   * @param versionB the older version
   *
   * @return true if version <tt>A</tt> is backwards compatible with version <tt>B</tt>
   */
  @VisibleForTesting
  static boolean isCompatible(Version versionA, Version versionB) {
    return versionA.equals(versionB)
        || (versionA.getMajorVersion() == versionB.getMajorVersion() && versionA.compareTo(versionB) >= 0);
  }
}
