package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializers;
import edu.wpi.first.shuffleboard.api.widget.Widgets;

import java.lang.reflect.Modifier;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PluginLoader {

  private static final PluginLoader defaultLoader = new PluginLoader();

  private DashboardTabPane dashboard;
  private final ObservableList<Plugin> loadedPlugins = FXCollections.observableArrayList();

  public static PluginLoader getDefault() {
    return defaultLoader;
  }

  public void setDashboard(DashboardTabPane dashboard) {
    this.dashboard = dashboard;
    loadedPlugins.forEach(p -> p.setDashboard(dashboard));
  }

  /**
   * Loads a plugin jar and loads all plugin classes within.
   *
   * @param jarFile the jar file to load plugins from
   */
  public void loadPluginJar(JarFile jarFile) {
    jarFile.stream()
        .filter(e -> e.getName().endsWith(".class"))
        .map(e -> e.getName().replace('/', '.'))
        .flatMap(this::tryLoadClass)
        .forEach(this::loadPluginClass);
  }

  private Stream<Class<?>> tryLoadClass(String name) {
    try {
      return Stream.of(ClassLoader.getSystemClassLoader().loadClass(name));
    } catch (ClassNotFoundException e) {
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
          // Unload existing plugin, if it exists
          loadedPlugins.stream()
              .filter(p -> p.getClass().getName().equals(clazz.getName()))
              .forEach(this::unload);
          Plugin plugin = (Plugin) clazz.newInstance();
          load(plugin);
          return true;
        }
      } catch (ReflectiveOperationException e) {
        // TODO log it?
        //e.printStackTrace();
        return false;
      }
    }
    return false;
  }

  /**
   * Loads a plugin.
   *
   * @param plugin the plugin to load
   */
  public void load(Plugin plugin) {
    plugin.setDashboard(this.dashboard);
    plugin.getDataTypes().forEach(DataTypes::register);
    plugin.getSourceTypes().forEach(SourceTypes::register);
    plugin.getTypeAdapters().forEach(Serializers::add);
    plugin.getWidgets().forEach(Widgets::register);

    plugin.onLoad();
    plugin.setLoaded(true);

    loadedPlugins.add(plugin);
  }

  /**
   * Unloads a plugin.
   *
   * @param plugin the plugin to unload
   */
  public void unload(Plugin plugin) {
    // TODO:
    //plugin.getWidgets().forEach(Widgets::unregister);
    //plugin.getSourceTypes().forEach(SourceTypes::unregister);
    //plugin.getDataTypes().forEach(DataTypes::unregister);

    plugin.onUnload();
    plugin.setLoaded(false);

    loadedPlugins.remove(plugin);
  }

  /**
   * Gets a list of all loaded plugins.
   */
  public ObservableList<Plugin> getLoadedPlugins() {
    return loadedPlugins;
  }

}
