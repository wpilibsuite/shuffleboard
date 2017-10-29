package edu.wpi.first.shuffleboard.api.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;

import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * <p>Common superclass for plugins that can be loaded by the app at startup or during runtime. Subclasses must have
 * public no-arg constructor or they will not be loaded.</p>
 *
 * <p>Shuffleboard will load plugins from all jars found in in the {@link Storage#PLUGINS_DIR plugin directory}.
 * This will overwrite pre-existing plugins with the same ID string (eg "edu.wpi.first.shuffleboard.Base") in
 * encounter order, which is alphabetical by jar name. For example, if a jar file "my_plugins.jar" defines a plugin
 * with ID "foo.bar" and another jar file "more_plugins.jar" <i>also</i> defines a plugin with that ID, the plugin
 * from "more_plugins" will be loaded first, then unloaded and replaced with the one from "my_plugins.jar". For this
 * reason, plugin authors should be careful to use unique group IDs. We recommend Java's reverse-DNS naming scheme.</p>
 */
public class Plugin {

  private final String groupId;
  private final String name;
  private final String version;
  private final String description;
  private final BooleanProperty loaded = new SimpleBooleanProperty(this, "loaded", false);

  protected Plugin(String groupId, String name, String version, String description) {
    this.groupId = groupId;
    this.name = name;
    this.version = version;
    this.description = description;
  }

  /**
   * The group ID of this plugin. For example, the stock plugins have the group ID {@code edu.wpi.first.shuffleboard}.
   * This must combine with {@link #getName()} to create a unique identifier for this plugin.
   */
  public final String getGroupId() {
    return groupId;
  }

  /**
   * Gets the name of this plugin.
   */
  public final String getName() {
    return name;
  }

  /**
   * Gets the version of this plugin. API consumers are strongly recommended to use
   * <a href="http://semver.org">semantic versioning</a>, but any versioning scheme may be used.
   */
  public final String getVersion() {
    return version;
  }

  /**
   * Gets an ID string unique to this plugin in the format {@code "{groupId}.{name}"}.
   */
  public final String idString() {
    return groupId + "." + name;
  }

  /**
   * Gets an ID string unique to this plugin in the format {@code "{groupId}.{name}-v{version}}. For example,
   * "foo.bar-v1.0.0".
   */
  public final String fullIdString() {
    return groupId + "." + name + "-v" + version;
  }

  /**
   * Gets a descriptive string describing what this plugin provides.
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Called when a plugin is loaded. Defaults to do nothing; plugins that require logic to be performed when they're
   * loaded (for example, connecting to a server) should be run here.
   */
  public void onLoad() {
    // Default to NOP
  }

  /**
   * Called when a plugin is unloaded.
   */
  public void onUnload() {
    // Default to NOP
  }

  /**
   * Gets a list of custom data types that this plugin defines.
   */
  public List<DataType> getDataTypes() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of custom source types that this plugin defines.
   */
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of the non-annotated components defined by this plugin.
   */
  public List<ComponentType> getComponents() {
    return ImmutableList.of();
  }

  /**
   * Gets a map of the default components to use for each data type.
   */
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of();
  }

  public List<TypeAdapter> getTypeAdapters() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of themes that this plugin defines.
   */
  public List<Theme> getThemes() {
    return ImmutableList.of();
  }

  /**
   * Gets a list of properties of this plugin that can be changed by users. Properties that are sensitive to rapid
   * changes (for example, a server URI that will attempt a connection on a change) should be wrapped in a
   * {@link edu.wpi.first.shuffleboard.api.prefs.FlushableProperty FlushableProperty} to ensure that a change will only
   * occur when a user manually confirms the change.
   */
  public List<Property<?>> getProperties() {
    return ImmutableList.of();
  }

  /**
   * Checks if this plugin has been loaded.
   */
  public final boolean isLoaded() {
    return loaded.get();
  }

  public final BooleanProperty loadedProperty() {
    return loaded;
  }

  /**
   * Flags this plugin as loaded or unloaded. <i>This does not perform any loading or unloading</i>, it merely sets the
   * flag. This is used by the plugin loader to set the flag after executing the load/unload logic.
   */
  public final void setLoaded(boolean loaded) {
    this.loaded.set(loaded);
  }

}
