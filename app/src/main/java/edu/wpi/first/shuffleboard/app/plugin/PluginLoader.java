package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializers;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.Widgets;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PluginLoader {

  private static final Logger log = Logger.getLogger(PluginLoader.class.getName());

  private static final PluginLoader defaultLoader = new PluginLoader();

  private final Set<Plugin> loadedPlugins = new HashSet<>();
  private final ObservableList<Plugin> knownPlugins = FXCollections.observableArrayList();

  public static PluginLoader getDefault() {
    return defaultLoader;
  }

  /**
   * Loads a plugin jar and loads all plugin classes within.
   *
   * @param jarUri a URI representing  jar file to load plugins from
   *
   * @throws IOException if the jar file denoted by the URI could not be found or read
   */
  public void loadPluginJar(URI jarUri) throws IOException {
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
          .forEach(this::loadPluginClass);
    }
  }

  private Stream<Class<?>> tryLoadClass(String name, ClassLoader classLoader) {
    try {
      return Stream.of(Class.forName(name, false, classLoader));
    } catch (ClassNotFoundException e) {
      // TODO log
      return Stream.empty();
    }
  }

  /**
   * Attempts to load a plugin class. This class may or not be a plugin; only a plugin class will be loaded.
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
          // Unload existing plugin, if it exists
          loadedPlugins.stream()
              .filter(p -> p.idString().equals(plugin.idString()))
              .findFirst()
              .ifPresent(oldPlugin -> {
                unload(oldPlugin);
                knownPlugins.remove(oldPlugin);
              });
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

  /**
   * Loads a plugin.
   *
   * @param plugin the plugin to load
   *
   * @throws IllegalArgumentException if a plugin has already been loaded with the same name
   */
  public void load(Plugin plugin) {
    if (loadedPlugins.contains(plugin)) {
      throw new IllegalArgumentException("The plugin " + plugin + " is already loaded");
    }
    log.info("Loading plugin " + plugin.fullIdString()); //NOPMD log not in if statement
    plugin.getDataTypes().forEach(DataTypes.getDefault()::register);
    plugin.getSourceTypes().forEach(SourceTypes.getDefault()::register);
    plugin.getTypeAdapters().forEach(Serializers::add);
    plugin.getWidgets().forEach(Widgets.getDefault()::register);
    plugin.getDefaultWidgets().forEach(Widgets.getDefault()::setDefaultWidget);
    Widgets.getDefault().getActiveWidgets().stream()
        .filter(w -> w.getSource() instanceof DestroyedSource)
        .filter(w -> {
          DataSource<?> source = w.getSource();
          return plugin.getWidgets().contains(w.getClass())
              || plugin.getDataTypes().contains(source.getDataType())
              || plugin.getSourceTypes().contains(source.getType());
        })
        .filter(w -> SourceTypes.getDefault().isRegistered(w.getSource().getType()))
        .forEach(w -> tryRestoreSource(w, (DestroyedSource) w.getSource()));
    plugin.getThemes().forEach(Themes.getDefault()::register);

    plugin.onLoad();
    plugin.setLoaded(true);

    loadedPlugins.add(plugin);
    if (!knownPlugins.contains(plugin)) {
      knownPlugins.add(plugin);
    }
  }

  private void tryRestoreSource(Widget widget, DestroyedSource destroyedSource) {
    try {
      widget.setSource(destroyedSource.restore());
    } catch (IncompatibleSourceException e) {
      log.fine("Could not set the restored source of " + widget // NOPMD log not in if statement
          + ". The plugin defining its data type was probably unloaded.");
    }
  }

  /**
   * Unloads a plugin.
   *
   * @param plugin the plugin to unload
   */
  public void unload(Plugin plugin) {
    log.info("Unloading plugin " + plugin.fullIdString()); // NOPMD log not in if statement
    Widgets.getDefault().getActiveWidgets().stream()
        .filter(w -> !(w.getSource() instanceof DestroyedSource))
        .filter(w -> {
          DataSource<?> source = w.getSource();
          return plugin.getDataTypes().contains(source.getDataType())
              || plugin.getSourceTypes().contains(source.getType());
        })
        .forEach(w -> w.setSource(new DestroyedSource<>(w.getSource())));
    plugin.getWidgets().forEach(Widgets.getDefault()::unregister);
    plugin.getSourceTypes().forEach(SourceTypes.getDefault()::unregister);
    plugin.getTypeAdapters().forEach(Serializers::remove);
    plugin.getDataTypes().forEach(DataTypes.getDefault()::unregister);
    // TODO figure out a good way to remember the theme & reapply it when reloading the plugin
    plugin.getThemes().forEach(Themes.getDefault()::unregister);

    plugin.onUnload();
    plugin.setLoaded(false);
    loadedPlugins.remove(plugin);
  }

  /**
   * Gets a list of all known plugins. These plugins may or not be loaded.
   */
  public ObservableList<Plugin> getKnownPlugins() {
    return knownPlugins;
  }

}
