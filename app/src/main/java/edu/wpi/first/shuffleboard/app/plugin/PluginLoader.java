package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializers;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.SingleSourceWidget;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

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
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class PluginLoader {

  private static final Logger log = Logger.getLogger(PluginLoader.class.getName());

  private static final PluginLoader defaultLoader =
      new PluginLoader(DataTypes.getDefault(), SourceTypes.getDefault(), Components.getDefault(), Themes.getDefault());

  private final ObservableSet<Plugin> loadedPlugins = FXCollections.observableSet();
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
   * Loads a plugin jar and loads all plugin classes within. Plugins will be loaded alphabetically by class name.
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
      jarFile.stream()
          .filter(e -> e.getName().endsWith(".class"))
          .map(e -> e.getName().replace('/', '.'))
          .map(n -> n.substring(0, n.length() - 6)) // ".class".length() == 6
          .flatMap(className -> tryLoadClass(className, classLoader))
          .filter(Plugin.class::isAssignableFrom)
          .map(c -> (Class<? extends Plugin>) c)
          .flatMap(c -> {
            try {
              return Stream.of(TypeUtils.tryInstantiate(c));
            } catch (ReflectiveOperationException e) {
              log.log(Level.WARNING, "Plugin class could not be loaded: " + c.getName(), e);
              return Stream.empty();
            }
          })
          .sorted(Comparator.<Plugin>comparingInt(p -> p.getDependencies().size())
              .thenComparing(PluginLoader::comparePluginsByDependencyGraph))
          .forEach(this::load);
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
  static int comparePluginsByDependencyGraph(Plugin p1, Plugin p2) {
    if (p1.dependsOn(p2)) {
      if (p2.dependsOn(p1)) {
        throw new IllegalStateException(
            "Cyclical dependency detected! " + p1.fullIdString() + " <-> " + p2.fullIdString());
      }
      return 1;
    } else if (p2.dependsOn(p1)) {
      return -1;
    } else {
      // No dependencies
      return 0;
    }
  }

  private Stream<Class<?>> tryLoadClass(String name, ClassLoader classLoader) {
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
  public boolean loadPluginClass(Class<?> clazz) {
    if (Plugin.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
      try {
        if (Modifier.isPublic(clazz.getConstructor().getModifiers())) {
          Plugin plugin = (Plugin) clazz.newInstance();
          load(plugin);
          return true;
        }
      } catch (ReflectiveOperationException e) {
        log.log(Level.WARNING, "Could not load plugin class", e);
        return false;
      }
    }
    return false;
  }

  private void unloadOldVersion(Plugin plugin) {
    loadedPlugins.stream()
        .filter(p -> p.idString().equals(plugin.idString()))
        .filter(p -> !p.getArtifact().getVersion().equals(plugin.getArtifact().getVersion()))
        .findFirst()
        .ifPresent(oldPlugin -> {
          unload(oldPlugin);
          knownPlugins.remove(oldPlugin);
        });
  }

  /**
   * Loads a plugin.
   *
   * @param plugin the plugin to load
   *
   * @return true if the plugin was loaded, false if it wasn't. This could happen if the plugin were already loaded,
   *         or if the plugin has unloaded dependencies.
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

    if (!canLoad(plugin)) {
      log.warning("Not all dependencies are present for " + plugin.getArtifact().toGradleString());
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
   * Checks if the given plugin can be loaded. A plugin is considered to be <i>loadable</i> if all plugins specified in
   * its {@link Plugin#getDependencies() dependencies list} are loaded and have a version of at least the one specified
   * in the dependency. For example, a plugin that depends on {@code "com.acme:PerfectPlugin:4.2.0"} is loadable
   * if a plugin {@code "com.acme:PerfectPlugin:5.3.1"} is loaded, but is not loadable if a plugin
   * {@code "com.acme:PerfectPlugin:0.1.0"} is loaded. Plugins with no dependencies are always loadable.
   *
   * @param plugin the plugin to check
   *
   * @return true if the plugin can be loaded, false if not
   */
  public boolean canLoad(Plugin plugin) {
    return plugin != null
        && plugin.getDependencies().stream()
        .allMatch(a ->
            loadedPlugins.stream()
                .filter(p -> p.idString().equals(a.getIdString()))
                .anyMatch(p -> p.getArtifact().getVersion().sameOrGreaterThan(a.getVersion()))
        );
  }

  private void tryRestoreSource(Widget widget, DestroyedSource destroyedSource) {
    try {
      widget.addSource(destroyedSource.restore());
    } catch (IncompatibleSourceException e) {
      log.fine("Could not set the restored source of " + widget
          + ". The plugin defining its data type was probably unloaded.");
    }
  }

  /**
   * Unloads a plugin.
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
    // Unload any dependent plugins first
    knownPlugins.stream()
        .filter(p -> p.dependsOn(plugin))
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
