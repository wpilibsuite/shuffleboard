package edu.wpi.first.shuffleboard.prefs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.theme.Theme;
import edu.wpi.first.shuffleboard.theme.ThemeManager;

import javafx.beans.property.Property;

/**
 * Contains the user preferences for the app. These preferences are user-specific and are saved
 * to the users home directory and are not contained in save files.
 */
public final class AppPreferences {

  private final Property<Theme> theme = ThemeManager.getInstance().themeProperty();

  @VisibleForTesting
  static AppPreferences instance = new AppPreferences();

  public static AppPreferences getInstance() {
    return instance;
  }

  /**
   * Gets a read-only list of all the preference properties.
   */
  public ImmutableList<Property<?>> getProperties() {
    return ImmutableList.of(
        theme
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

}
