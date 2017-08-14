package edu.wpi.first.shuffleboard.api.theme;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Keeps track of the themes available to the application.
 */
public final class Themes {

  private static final ObservableList<Theme> themes = FXCollections.observableArrayList();

  /**
   * The default "Modena" theme bundled with JavaFX. This theme cannot be unregistered.
   */
  public static final Theme MODENA = new Theme("Modena");

  static {
    register(MODENA);
  }

  private Themes() {
    throw new UnsupportedOperationException("Themes is a utility class!");
  }

  /**
   * Gets the theme with the given name. If there is no theme with that name, returns
   * {@link #MODENA} instead.
   *
   * @param name the name of the theme to get
   */
  public static Theme forName(String name) {
    return themes.stream()
        .filter(t -> t.getName().equals(name))
        .findFirst()
        .orElse(MODENA);
  }

  public static void register(Theme theme) {
    if (themes.contains(theme)) {
      throw new IllegalArgumentException("Theme " + theme.getName() + " has already been registered");
    }
    themes.add(theme);
  }

  public static void unregister(Theme theme) {
    if (theme == MODENA) {
      throw new IllegalArgumentException("The modena theme cannot be unregistered");
    }
    themes.remove(theme);
  }

  public static ObservableList<Theme> getThemes() {
    return themes;
  }

}
