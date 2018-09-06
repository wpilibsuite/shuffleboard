package edu.wpi.first.shuffleboard.api.theme;

import edu.wpi.first.shuffleboard.api.util.Registry;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;

/**
 * Keeps track of the themes available to the application.
 */
public final class Themes extends Registry<Theme> {

  private static final Logger log = Logger.getLogger(Themes.class.getName());

  // TODO replace with DI eg Guice
  private static Themes defaultInstance;

  public static final Theme MATERIAL_LIGHT = new Theme("Material Light", "/edu/wpi/first/shuffleboard/app/light.css");
  public static final Theme MATERIAL_DARK = new Theme("Material Dark", "/edu/wpi/first/shuffleboard/app/dark.css");
  public static final Theme MIDNIGHT = new Theme("Midnight", "/edu/wpi/first/shuffleboard/app/midnight.css");

  public static final Theme INITIAL_THEME = MATERIAL_LIGHT;

  /**
   * Gets the default themes instance.
   */
  public static Themes getDefault() {
    synchronized (Themes.class) {
      if (defaultInstance == null) {
        defaultInstance = new Themes(MATERIAL_LIGHT, MATERIAL_DARK, MIDNIGHT);
      }
    }
    return defaultInstance;
  }

  /**
   * Creates a new theme registry.
   *
   * @param initial the initial themes
   */
  public Themes(Theme... initial) {
    registerAll(initial);
  }

  /**
   * Gets the theme with the given name. If there is no theme with that name, returns
   * {@link #INITIAL_THEME} instead.
   *
   * @param name the name of the theme to get
   */
  public Theme forName(String name) {
    return getItems().stream()
        .filter(t -> t.getName().equals(name))
        .findFirst()
        .orElse(INITIAL_THEME);
  }

  @Override
  public void register(Theme theme) {
    Objects.requireNonNull(theme, "theme");
    if (isRegistered(theme)) {
      throw new IllegalArgumentException("Theme " + theme + " is already registered");
    }
    addItem(theme);
  }

  @Override
  public void unregister(Theme theme) {
    removeItem(theme);
  }

  /**
   * Gets an observable list of the registered themes.
   */
  public ObservableList<Theme> getThemes() {
    return getItems();
  }

  /**
   * Loads and registers all themes from the {@link Storage#getThemesDir() themes directory}.
   *
   * @throws IOException if the themes directory does not exist and could not be created
   * @throws IOException if the themes directory exists but could not be read from
   */
  public void loadThemesFromDir() throws IOException {
    Path themesPath = Storage.getThemesDir();
    Files.list(themesPath)
        .filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
        .map(this::loadThemeFromDir)
        .flatMap(TypeUtils.optionalStream())
        .forEach(this::register);
  }

  private Optional<Theme> loadThemeFromDir(Path dir) {
    try {
      return Optional.of(new Theme(dir.getFileName().toString(), getStyleSheetsInPath(dir)));
    } catch (IOException e) {
      log.log(Level.WARNING, "Themes could not be loaded from directory: " + dir.toAbsolutePath(), e);
      return Optional.empty();
    }
  }

  /**
   * Gets an array of the stylesheets in a theme directory.
   *
   * @param dir the directory in which to search for stylesheets.
   */
  private String[] getStyleSheetsInPath(Path dir) throws IOException {
    return Files.list(dir)
        .filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
        .map(Path::toAbsolutePath)
        .map(this::toExternalForm)
        .flatMap(TypeUtils.optionalStream())
        .toArray(String[]::new);
  }

  private Optional<String> toExternalForm(Path path) {
    try {
      return Optional.of(path.toUri().toURL().toExternalForm());
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "Could not get external form of " + path, e);
      return Optional.empty();
    }
  }

}
