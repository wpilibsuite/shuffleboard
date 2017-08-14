package edu.wpi.first.shuffleboard.api.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Common superclass for plugins that can be loaded by the app at startup or during runtime. Subclasses must have
 * public no-arg constructor or they will not be loaded.
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
   * Gets a list of the widget types that this plugin defines.
   */
  public List<Class<? extends Widget>> getWidgets() {
    return ImmutableList.of();
  }

  /**
   * Gets a map of the default widgets this plugin defines.
   */
  public Map<DataType, Class<? extends Widget>> getDefaultWidgets() {
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

  public boolean isLoaded() {
    return loaded.get();
  }

  public BooleanProperty loadedProperty() {
    return loaded;
  }

  public void setLoaded(boolean loaded) {
    this.loaded.set(loaded);
  }

}
