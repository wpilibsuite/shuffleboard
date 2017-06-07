package edu.wpi.first.shuffleboard.prefs;

import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.theme.Theme;
import edu.wpi.first.shuffleboard.theme.ThemeManager;

import javafx.beans.property.Property;

/**
 * Contains the user preferences for the app. These preferences are user-specific and are saved
 * to the users home directory and are not contained in save files.
 */
public final class AppPreferences {

  private static Property<Theme> theme = ThemeManager.themeProperty();

  private AppPreferences() {
  }

  /**
   * Gets a read-only list of all the preference properties.
   */
  public static ImmutableList<Property<?>> getProperties() {
    return ImmutableList.of(
        theme
    );
  }

  public static Property<Theme> themeProperty() {
    return theme;
  }

  public static Theme getTheme() {
    return theme.getValue();
  }

  public static void setTheme(Theme theme) {
    AppPreferences.theme.setValue(theme);
  }

}
