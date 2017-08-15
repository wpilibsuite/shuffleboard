package edu.wpi.first.shuffleboard.api.theme;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Keeps track of the themes available to the application.
 */
public final class Themes {

  private static final ObservableList<Theme> themes = FXCollections.observableArrayList();

  public static final Theme MATERIAL_LIGHT = new Theme("Material Light", "/edu/wpi/first/shuffleboard/app/light.css");
  public static final Theme MATERIAL_DARK = new Theme("Material Dark", "/edu/wpi/first/shuffleboard/app/dark.css");

  public static final Theme INITIAL_THEME = MATERIAL_LIGHT;

  static {
    register(MATERIAL_LIGHT);
    register(MATERIAL_DARK);
  }

  private Themes() {
    throw new UnsupportedOperationException("Themes is a utility class!");
  }

  /**
   * Gets the theme with the given name. If there is no theme with that name, returns
   * {@link #INITIAL_THEME} instead.
   *
   * @param name the name of the theme to get
   */
  public static Theme forName(String name) {
    return themes.stream()
        .filter(t -> t.getName().equals(name))
        .findFirst()
        .orElse(INITIAL_THEME);
  }

  /**
   * Registers a theme.
   *
   * @param theme the theme to register
   */
  public static void register(Theme theme) {
    if (!themes.contains(theme)) {
      themes.add(theme);
    }
  }

  /**
   * Unregisters a theme.
   *
   * @param theme the theme to unregister
   */
  public static void unregister(Theme theme) {
    themes.remove(theme);
  }

  /**
   * Gets an observable list of the registered themes.
   */
  public static ObservableList<Theme> getThemes() {
    return themes;
  }

}
