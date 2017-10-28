package edu.wpi.first.shuffleboard.api.plugin;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Common superclass for plugins that can be loaded by the app at startup or during runtime. Subclasses must have
 * public no-arg constructor or they will not be loaded.
 */
public class Plugin {

  private final String groupId;
  private final String name;
  private final String version;
  private final PluginArtifact artifact;
  private final String description;
  private final BooleanProperty loaded = new SimpleBooleanProperty(this, "loaded", false);
  private final List<PluginArtifact> dependencies = new ArrayList<>();

  /**
   * Creates a new plugin instance.
   *
   * @param groupId     the ID of the group developing the plugin (eg "edu.wpi.first")
   * @param name        the name of the plugin
   * @param version     the current version of the plugin. This must follow <a href="http://semver.org">Semantic Versioning</a>
   * @param description a description of the plugin
   */
  protected Plugin(String groupId, String name, String version, String description) {
    this.groupId = groupId;
    this.name = name;
    this.version = version;
    this.artifact = new PluginArtifact(groupId, name, version);
    this.description = description;
  }

  /**
   * Adds a dependency on another plugin. If no plugin matching the artifact is loaded, this plugin will be prevented
   * from being loaded.
   *
   * @param artifact the plugin artifact describing the plugin that this depends on
   */
  protected final void addDependency(PluginArtifact artifact) {
    dependencies.add(artifact);
  }

  /**
   * Adds a dependency on another plugin. If no matching plugin is loaded, this plugin will be prevented from being
   * loaded.
   *
   * @param groupId the group ID of the plugin to add a dependency for
   * @param name    the name of the plugin
   * @param version the earliest supported plugin version
   */
  protected final void addDependency(String groupId, String name, String version) {
    addDependency(new PluginArtifact(groupId, name, version));
  }

  /**
   * Adds a dependency on a plugin using a gradle-style dependency string in the format
   * {@code "{groupId}:{name}:{version}"}.
   *
   * @throws IllegalArgumentException if the string does not follow gradle-style dependency string formatting rules
   */
  protected final void addDependency(String depString) {
    String[] parts = depString.split(":");
    if (parts.length != 3) {
      throw new IllegalArgumentException("The dependency string was not in the correct format");
    }
    addDependency(parts[0], parts[1], parts[2]);
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

  public PluginArtifact getArtifact() {
    return artifact;
  }

  /**
   * Gets an ID string unique to this plugin in the format {@code "{groupId}:{name}"}.
   */
  public final String idString() {
    return artifact.getIdString();
  }

  /**
   * Gets an ID string unique to this plugin in the format {@code "{groupId}:{name}:{version}}. For example,
   * "foo.bar:baz:1.0.0".
   */
  public final String fullIdString() {
    return artifact.toGradleString();
  }

  /**
   * Gets a descriptive string describing what this plugin provides.
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Gets a list of plugins that this plugin depends on. If matching plugins are not loaded, this plugin will not be
   * able to be loaded.
   */
  public final List<PluginArtifact> getDependencies() {
    return ImmutableList.copyOf(dependencies);
  }

  /**
   * Checks if this plugin depends upon another. If that plugin is not loaded, this one will be unable to be loaded.
   *
   * @param plugin the plugin to check for a dependency on
   */
  public final boolean dependsOn(Plugin plugin) {
    return getDependencies().stream().anyMatch(artifact -> artifact.getIdString().equals(plugin.idString()));
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

  @Override
  public final boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!obj.getClass().equals(this.getClass())) {
      return false;
    }
    return ((Plugin) obj).getArtifact().equals(this.getArtifact());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(groupId, name, version);
  }

}
