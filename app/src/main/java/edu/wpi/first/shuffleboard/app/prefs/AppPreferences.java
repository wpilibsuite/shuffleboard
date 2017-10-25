package edu.wpi.first.shuffleboard.app.prefs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.util.PreferencesUtils;

import java.util.prefs.Preferences;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Contains the user preferences for the app. These preferences are user-specific and are saved
 * to the users home directory and are not contained in save files.
 */
public final class AppPreferences {

  private final Property<Theme> theme = new SimpleObjectProperty<>(this, "Theme", Themes.INITIAL_THEME);
  private final DoubleProperty defaultTileSize = new SimpleDoubleProperty(this, "defaultTileSize", 128);

  @VisibleForTesting
  static AppPreferences instance = new AppPreferences();

  /**
   * Creates a new app preferences instance.
   */
  public AppPreferences() {
    Preferences preferences = Preferences.userNodeForPackage(getClass());
    PreferencesUtils.read(theme, preferences, Themes.getDefault()::forName);
    PreferencesUtils.read(defaultTileSize, preferences);

    theme.addListener(__ -> PreferencesUtils.save(theme, preferences, Theme::getName));
    defaultTileSize.addListener(__ -> PreferencesUtils.save(defaultTileSize, preferences));
  }

  public static AppPreferences getInstance() {
    return instance;
  }

  /**
   * Gets a read-only list of all the preference properties.
   */
  public ImmutableList<Property<?>> getProperties() {
    return ImmutableList.of(
        theme,
        defaultTileSize
    );
  }

  public Property<Theme> themeProperty() {
    return theme;
  }

  public Theme getTheme() {
    return theme.getValue();
  }

  public void setTheme(Theme theme) {
    this.theme.setValue(theme);
  }

  public double getDefaultTileSize() {
    return defaultTileSize.get();
  }

  public DoubleProperty defaultTileSizeProperty() {
    return defaultTileSize;
  }

  public void setDefaultTileSize(double defaultTileSize) {
    this.defaultTileSize.set(defaultTileSize);
  }

}
