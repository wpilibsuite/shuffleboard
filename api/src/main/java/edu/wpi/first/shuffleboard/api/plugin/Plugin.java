package edu.wpi.first.shuffleboard.api.plugin;

import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.api.Dashboard;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Common superclass for plugins that can be loaded by the app at startup or during runtime. Subclasses must have
 * public no-arg constructor or they will not be loaded.
 */
public class Plugin {

  private final String name;
  private Dashboard dashboard = null;
  private final BooleanProperty loaded = new SimpleBooleanProperty(this, "loaded", false);

  protected Plugin(String name) {
    this.name = name;
  }

  /**
   * Gets the name of this plugin.
   */
  public final String getName() {
    return name;
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

  public List<TypeAdapter> getTypeAdapters() {
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

  public void setDashboard(Dashboard dashboard) {
    this.dashboard = dashboard;
  }

  public Dashboard getDashboard() {
    return dashboard;
  }
}
