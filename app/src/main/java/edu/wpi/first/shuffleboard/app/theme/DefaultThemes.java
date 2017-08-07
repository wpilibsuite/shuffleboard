package edu.wpi.first.shuffleboard.app.theme;

import java.util.Arrays;

/**
 * Contains the default themes bundled with the application.
 */
public final class DefaultThemes {

  /**
   * The default, "light" theme.
   */
  public static final Theme LIGHT = new Theme("Light", "/edu/wpi/first/shuffleboard/light.css");

  /**
   * The "dark" theme.
   */
  public static final Theme DARK = new Theme("Dark", "/edu/wpi/first/shuffleboard/dark.css");

  private DefaultThemes() {
  }

  /**
   * Gets an array of all the default themes.
   */
  public static Theme[] getThemes() {
    return new Theme[]{
        LIGHT,
        DARK
    };
  }

  /**
   * Gets the theme with the given name. If there is no theme with that name, returns
   * {@code defaultTheme} instead.
   *
   * @param name         the name of the theme to get
   * @param defaultTheme the theme to return if no theme exists with the given name
   */
  public static Theme forName(String name, Theme defaultTheme) {
    return Arrays.stream(getThemes())
        .filter(theme -> theme.getName().equals(name))
        .findFirst()
        .orElse(defaultTheme);
  }

}
