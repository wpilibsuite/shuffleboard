package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.InvalidPluginDefinitionException;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.plugin.Requirements;
import edu.wpi.first.shuffleboard.api.plugin.Requires;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializers;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.SingleSourceWidget;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.sources.DataTypeChangedException;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import com.cedarsoft.version.Version;
import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class PluginLoader {

  private static final Logger log = Logger.getLogger(PluginLoader.class.getName());

  private static final PluginLoader defaultLoader =
      new PluginLoader(DataTypes.getDefault(), SourceTypes.getDefault(), Components.getDefault(), Themes.getDefault());

  private final ObservableSet<Plugin> loadedPlugins = FXCollections.observableSet();
  private final Set<Class<? extends Plugin>> knownPluginClasses = new HashSet<>();
  private final ObservableList<Plugin> knownPlugins = FXCollections.observableArrayList();
  private final DataTypes dataTypes;
  private final SourceTypes sourceTypes;
  private final Components components;
  private final Themes themes;

  /**
   * Creates a new plugin loader object. For app use, use {@link #getDefault() the default instance}; this should only
   * be used for tests.
   *
   * @param dataTypes   the data type registry to use for registering data types from plugins
   * @param sourceTypes the source type registry to use for registering source types from plugins
   * @param components  the component registry to use for registering components from plugins
   * @param themes      the theme registry to use for registering themes from plugins
   *
   * @throws NullPointerException if any of the parameters is {@code null}
   */
  public PluginLoader(DataTypes dataTypes, SourceTypes sourceTypes, Components components, Themes themes) {
    this.dataTypes = Objects.requireNonNull(dataTypes, "dataTypes");
    this.sourceTypes = Objects.requireNonNull(sourceTypes, "sourceTypes");
    this.components = Objects.requireNonNull(components, "components");
    this.themes = Objects.requireNonNull(themes, "themes");
  }

  /**
   * Gets the default plugin loader instance. This should be used as the global instance for use in the application.
   */
  public static PluginLoader getDefault() {
    return defaultLoader;
  }

  /**
   * Loads all jars found in the given directory. This does not load jars in nested directories. Jars will be loaded in
   * alphabetical order.
   *
   * @param directory the directory to load plugins from
   *
   * @throws IllegalArgumentException if the path is not a directory
   * @throws IOException              if the directory could not be read from
   * @see #loadPluginJar
   */
  public void loadAllJarsFromDir(Path directory) throws IOException {
    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException("The given path is not a directory: " + directory);
    }
    Files.list(directory)
        .filter(p -> p.toString().endsWith(".jar"))
        .map(Path::toUri)
        .sorted() // sort alphabetically to make load order deterministic
        .forEach(jar -> {
          try {
            loadPluginJar(jar);
          } catch (IOException e) {
            log.log(Level.WARNING, "Could not load plugin jar: " + jar, e);
          }
        });
  }

  /**
   * Loads a plugin jar and loads all plugin classes within. Plugins will be loaded in the following order:
   *
   * <ol>
   * <li>By the amount of dependencies</li>
   * <li>By dependency graph; if one plugin requires another, the requirement will be loaded first</li>
   * <li>By class name</li>
   * </ol>
   *
   * @param jarUri a URI representing  jar file to load plugins from
   *
   * @throws IOException if the jar file denoted by the URI could not be found or read
   * @see #load(Plugin)
   */
  public void loadPluginJar(URI jarUri) throws IOException {
    log.info("Attempting to load plugin jar: " + jarUri);
    URL url = jarUri.toURL();
    PrivilegedAction<URLClassLoader> getClassLoader = () -> {
      return new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
    };
    URLClassLoader classLoader = AccessController.doPrivileged(getClassLoader);
    try (JarFile jarFile = new JarFile(new File(jarUri))) {
      List<? extends Class<? extends Plugin>> pluginClasses = jarFile.stream()
          .filter(e -> e.getName().endsWith(".class"))
          .map(e -> e.getName().replace('/', '.'))
          .map(n -> n.substring(0, n.length() - 6)) // ".class".length() == 6
          .flatMap(className -> tryLoadClass(className, classLoader))
          .filter(Plugin.class::isAssignableFrom)
          .map(c -> (Class<? extends Plugin>) c)
          .filter(c -> {
            try {
              Plugin.validatePluginClass(c);
              return true;
            } catch (InvalidPluginDefinitionException e) {
              log.log(Level.WARNING, "Invalid plugin class " + c.getName(), e);
              return false;
            }
          })
          .collect(Collectors.toList());
      knownPluginClasses.addAll(pluginClasses);
      pluginClasses.stream()
          .sorted(Comparator.<Class<? extends Plugin>>comparingInt(p -> getRequirements(p).size())
              .thenComparing(this::comparePluginsByDependencyGraph)
              .thenComparing(Comparator.comparing(Class::getName)))
          .forEach(this::loadPluginClass);
    }
  }

  /**
   * Compares plugins such that plugins with dependencies will be at the end of the list or array being sorted.
   *
   * @param p1 the first plugin to compare
   * @param p2 the second plugin to compare
   *
   * @throws IllegalStateException if the two plugins depend on each other, creating a cyclical dependency
   */
  @VisibleForTesting
  int comparePluginsByDependencyGraph(Class<? extends Plugin> p1, Class<? extends Plugin> p2) {
    if (requires(p1, p2)) {
      if (requires(p2, p1)) {
        throw new IllegalStateException(
            "Cyclical dependency detected! " + p1.getName() + " <-> " + p2.getName());
      }
      return 1;
    } else if (requires(p2, p1)) {
      return -1;
    } else {
      // No dependencies
      return 0;
    }
  }

  private static Stream<Class<?>> tryLoadClass(String name, ClassLoader classLoader) {
    try {
      return Stream.of(Class.forName(name, false, classLoader));
    } catch (ClassNotFoundException e) {
      log.log(Level.WARNING, "Could not load class for name '" + name + "' with classloader " + classLoader, e);
      return Stream.empty();
    }
  }

  /**
   * Attempts to load a plugin class. This class may or not be a plugin; only a plugin class will be loaded. A plugin
   * loaded with this method will be loaded after unloading a plugin that shares the same
   * {@link Plugin#idString() ID string}, if one exists.
   *
   * @param clazz the class to attempt to load
   *
   * @return true if the class is a plugin class and was successfully loaded; false otherwise
   */
  public boolean loadPluginClass(Class<? extends Plugin> clazz) {
    if (Plugin.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
      try {
        Plugin.validatePluginClass(clazz);
        if (Modifier.isPublic(clazz.getConstructor().getModifiers())) {
          Plugin plugin = clazz.newInstance();
          load(plugin);
          return true;
        }
      } catch (ReflectiveOperationException | InvalidPluginDefinitionException e) {
        log.log(Level.WARNING, "Could not load plugin class", e);
        return false;
      }
    }
    return false;
  }

  private void unloadOldVersion(Plugin plugin) {
    loadedPlugins.stream()
        .filter(p -> p.idString().equals(plugin.idString()))
        .filter(p -> !p.getVersion().equals(plugin.getVersion()))
        .findFirst()
        .ifPresent(oldPlugin -> {
          unload(oldPlugin);
          knownPlugins.remove(oldPlugin);
          knownPluginClasses.remove(plugin.getClass());
        });
  }

  /**
   * Loads a plugin.
   *
   * @param plugin the plugin to load
   *
   * @return true if the plugin was loaded, false if it wasn't. This could happen if the plugin were already loaded,
   *         or if the plugin requires other plugins that are not loaded.
   *
   * @throws IllegalArgumentException if a plugin already exists with the same ID
   */
  public boolean load(Plugin plugin) {
    if (loadedPlugins.contains(plugin)) {
      // Already loaded
      return false;
    }

    // Unload an already-loaded version of this plugin, if it exists
    // This allows us to load
    unloadOldVersion(plugin);
    knownPluginClasses.add(plugin.getClass());

    if (!canLoad(plugin)) {
      log.warning("Not all requirements are present for " + plugin.fullIdString());
      if (!knownPlugins.contains(plugin)) {
        knownPlugins.add(plugin);
      }
      return false;
    }
    log.info("Loading plugin " + plugin.fullIdString());
    plugin.getDataTypes().forEach(dataTypes::register);
    plugin.getSourceTypes().forEach(sourceTypes::register);
    plugin.getTypeAdapters().forEach(Serializers::add);
    plugin.getComponents().forEach(components::register);
    plugin.getDefaultComponents().forEach(components::setDefaultComponent);
    components.getActiveWidgets().stream()
        .filter(w -> w.getSources().stream().anyMatch(s -> s instanceof DestroyedSource))
        .filter(w -> {
          return plugin.getComponents().stream().anyMatch(t -> t.getType().equals(w.getClass()))
              || w.getSources().stream().anyMatch(s -> plugin.getDataTypes().contains(s.getDataType()))
              || w.getSources().stream().anyMatch(s -> plugin.getSourceTypes().contains(s.getType()));
        })
        .filter(w -> w.getSources().stream().anyMatch(s -> sourceTypes.isRegistered(s.getType())))
        .forEach(w -> {
          w.getSources().stream()
              .filter(s -> s instanceof DestroyedSource)
              .forEach(s -> tryRestoreSource(w, (DestroyedSource) s));
        });
    plugin.getThemes().forEach(themes::register);

    plugin.onLoad();
    plugin.setLoaded(true);

    loadedPlugins.add(plugin);
    if (!knownPlugins.contains(plugin)) {
      knownPlugins.add(plugin);
    }
    return true;
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
  private static Description getDescription(Class<? extends Plugin> pluginClass)
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
  private static List<Requires> getRequirements(Class<? extends Plugin> pluginClass) {
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
   * Checks if plugin <tt>A</tt> requires plugin <tt>B</tt>, either directly or through transitive dependencies.
   *
   * @param pluginA the plugin to check
   * @param pluginB the plugin to check as a possible requirement of plugin <tt>A</tt>
   *
   * @return true if plugin <tt>A</tt> requires plugin <tt>B</tt>, either directly or through transitive dependencies
   */
  private boolean requires(Class<? extends Plugin> pluginA, Class<? extends Plugin> pluginB) {
    return isDirectRequirement(pluginA, pluginB)
        || knownPluginClasses.stream()
        .filter(c -> isDirectRequirement(pluginA, c))
        .anyMatch(c -> requires(c, pluginB));
  }

  /**
   * Checks if a plugin <i>directly requires</i> on another.
   *
   * @param pluginA the plugin to check
   * @param pluginB the plugin to check as a possible requirement of plugin <tt>A</tt>
   *
   * @return true if plugin <tt>A</tt> directly requires plugin <tt>B</tt>, false if not
   */
  private static boolean isDirectRequirement(Class<? extends Plugin> pluginA, Class<? extends Plugin> pluginB) {
    Description description = getDescription(pluginB);
    return getRequirements(pluginA).stream()
        .filter(d -> d.group().equals(description.group()))
        .filter(d -> d.name().equals(description.name()))
        .map(d -> Version.parse(d.minVersion()))
        .anyMatch(v -> isCompatible(v, Version.parse(description.version())));
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
    return versionA.getMajor() == versionB.getMajor()
        && versionA.sameOrGreaterThan(versionB);
  }

  /**
   * Checks if the given plugin can be loaded. A plugin is considered to be <i>loadable</i> if all plugins specified in
   * its requirements are loaded and have a version of at least the one specified in the dependency, with the same major
   * version number. For example, a plugin that depends on {@code "com.acme:PerfectPlugin:4.2.0"} is loadable if a
   * plugin {@code "com.acme:PerfectPlugin:4.3.1"} is loaded, but is not loadable if a plugin
   * {@code "com.acme:PerfectPlugin:0.1.0"} or {@code "com.acme.PerfectPlugin:5.0.0} is loaded. Plugins with no
   * requirements are always loadable.
   *
   * @param plugin the plugin to check
   *
   * @return true if the plugin can be loaded, false if not
   */
  public boolean canLoad(Plugin plugin) {
    return plugin != null
        && getRequirements(plugin.getClass()).stream()
        .allMatch(a ->
            loadedPlugins.stream()
                .filter(p -> p.getGroupId().equals(a.group()) && p.getName().equals(a.name()))
                .anyMatch(p ->
                    isCompatible(
                        p.getVersion(),
                        Version.parse(a.minVersion())
                    )
                )
        );
  }

  private void tryRestoreSource(Widget widget, DestroyedSource destroyedSource) {
    try {
      DataSource restore = destroyedSource.restore();
      widget.addSource(restore);
      widget.removeSource(destroyedSource);
    } catch (IncompatibleSourceException | DataTypeChangedException e) {
      log.log(Level.WARNING, "Could not set the restored source of " + widget
          + ". The plugin defining its data type was probably unloaded.", e);
    }
  }

  /**
   * Unloads a plugin. Any plugins that require the one being unloaded will also be unloaded before unloading the given
   * one.
   *
   * @param plugin the plugin to unload
   *
   * @return true if the plugin was unloaded, false if not. This can occur if the plugin was not loaded to begin with.
   */
  @SuppressWarnings("unchecked")
  public boolean unload(Plugin plugin) {
    if (!loadedPlugins.contains(plugin)) {
      // It's not loaded, nothing to unload
      return false;
    }
    // Unload any plugins that depends on the one currently being unloaded
    knownPlugins.stream()
        .filter(p -> requires(p.getClass(), plugin.getClass()))
        .forEach(this::unload);

    log.info("Unloading plugin " + plugin.fullIdString());
    components.getActiveWidgets().stream()
        .filter(w -> w.getSources().stream().anyMatch(s -> !(s instanceof DestroyedSource)))
        .filter(w -> {
          return w.getSources().stream().anyMatch(s -> plugin.getDataTypes().contains(s.getDataType()))
              || w.getSources().stream().anyMatch(s -> plugin.getSourceTypes().contains(s.getType()));
        })
        .forEach(w -> {
          if (w instanceof SingleSourceWidget) {
            SingleSourceWidget singleSource = (SingleSourceWidget) w;
            singleSource.setSource(new DestroyedSource<>(singleSource.getSource()));
          } else {
            w.getSources().replaceAll(s -> {
              if (!(s instanceof DestroyedSource)
                  && (plugin.getDataTypes().contains(s.getDataType())
                  || plugin.getSourceTypes().contains(s.getType()))) {
                return new DestroyedSource<>(s);
              }
              return s;
            });
          }
        });
    plugin.getComponents().forEach(components::unregister);
    plugin.getSourceTypes().forEach(sourceTypes::unregister);
    plugin.getTypeAdapters().forEach(Serializers::remove);
    plugin.getDataTypes().forEach(dataTypes::unregister);
    // TODO figure out a good way to remember the theme & reapply it when reloading the plugin
    plugin.getThemes().forEach(themes::unregister);

    plugin.onUnload();
    plugin.setLoaded(false);
    loadedPlugins.remove(plugin);
    return true;
  }

  /**
   * Gets a list of all known plugins. These plugins may or not be loaded.
   */
  public ObservableList<Plugin> getKnownPlugins() {
    return knownPlugins;
  }

  public ObservableSet<Plugin> getLoadedPlugins() {
    return FXCollections.unmodifiableObservableSet(loadedPlugins);
  }
}
