package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializers;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.Widgets;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PluginLoader {

  private static final Logger log = Logger.getLogger(PluginLoader.class.getName());

  private static final PluginLoader defaultLoader = new PluginLoader();

  private final ObservableList<Plugin> knownPlugins = FXCollections.observableArrayList();

  public static PluginLoader getDefault() {
    return defaultLoader;
  }

  /**
   * Loads a plugin jar and loads all plugin classes within.
   *
   * @param jarFile the jar file to load plugins from
   */
  public void loadPluginJar(JarFile jarFile) throws MalformedURLException {
    URL url = new File(jarFile.getName()).toURI().toURL();
    URLClassLoader classLoader = AccessController.doPrivileged((PrivilegedAction<URLClassLoader>) () -> {
      return new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
    });

    jarFile.stream()
        .filter(e -> e.getName().endsWith(".class"))
        .map(e -> e.getName().replace('/', '.'))
        .map(n -> n.substring(0, n.length() - 6)) // ".class".length() == 6
        .flatMap(className -> tryLoadClass(className, classLoader))
        .forEach(this::loadPluginClass);
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
          knownPlugins.stream()
              .filter(p -> p.getName().equals(plugin.getName()))
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
    log.info("Loading plugin " + plugin.getName());
    plugin.getDataTypes().forEach(DataTypes::register);
    plugin.getSourceTypes().forEach(SourceTypes::register);
    plugin.getTypeAdapters().forEach(Serializers::add);
    plugin.getWidgets().forEach(Widgets::register);
    Widgets.getActiveWidgets().stream()
        .filter(w -> w.getSource() instanceof DestroyedSource)
        .filter(w -> {
          DataSource<?> source = w.getSource();
          return plugin.getWidgets().contains(w.getClass())
              || plugin.getDataTypes().contains(source.getDataType())
              || plugin.getSourceTypes().contains(source.getType());
        })
        .filter(w -> SourceTypes.isRegistered(w.getSource().getType()))
        .forEach(w -> tryRestoreSource(w, (DestroyedSource) w.getSource()));

    plugin.onLoad();
    plugin.setLoaded(true);

    if (!knownPlugins.contains(plugin)) {
      knownPlugins.add(plugin);
    }
  }

  private void tryRestoreSource(Widget widget, DestroyedSource destroyedSource) {
    try {
      widget.setSource(destroyedSource.restore());
    } catch (IncompatibleSourceException e) {
      log.fine("Could not set the restored source of " + widget +
          ". The plugin defining its data type was probably unloaded.");
    }
  }

  /**
   * Unloads a plugin.
   *
   * @param plugin the plugin to unload
   */
  public void unload(Plugin plugin) {
    log.info("Unloading plugin " + plugin.getName());
    Widgets.getActiveWidgets().stream()
        .filter(w -> !(w.getSource() instanceof DestroyedSource))
        .filter(w -> {
          DataSource<?> source = w.getSource();
          return plugin.getDataTypes().contains(source.getDataType())
              || plugin.getSourceTypes().contains(source.getType());
        })
        .forEach(w -> w.setSource(new DestroyedSource<>(w.getSource())));
    plugin.getWidgets().forEach(Widgets::unregister);
    plugin.getSourceTypes().forEach(SourceTypes::unregister);
    plugin.getTypeAdapters().forEach(Serializers::remove);
    plugin.getDataTypes().forEach(DataTypes::unregister);

    plugin.onUnload();
    plugin.setLoaded(false);
  }

  /**
   * Gets a list of all known plugins. These plugins may or not be loaded.
   */
  public ObservableList<Plugin> getKnownPlugins() {
    return knownPlugins;
  }

}
