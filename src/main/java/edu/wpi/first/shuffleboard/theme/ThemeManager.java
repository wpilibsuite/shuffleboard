package edu.wpi.first.shuffleboard.theme;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Manages the CSS themes for the application.
 */
public final class ThemeManager {

  private final Property<Theme> theme
      = new SimpleObjectProperty<>(ThemeManager.class, "Theme", DefaultThemes.LIGHT);

  private final Property<ImmutableList<String>> styleSheets
      = new SimpleObjectProperty<>(ThemeManager.class, "StyleSheets", ImmutableList.of());

  @VisibleForTesting
  static ThemeManager instance = new ThemeManager();

  private ThemeManager() {
    styleSheets.bind(Bindings.createObjectBinding(() -> getTheme().getStyleSheets(), theme));
  }

  public static ThemeManager getInstance() {
    return instance;
  }

  public ReadOnlyProperty<ImmutableList<String>> styleSheetsProperty() {
    return styleSheets;
  }

  /**
   * Gets the current application stylesheets.
   */
  public ImmutableList<String> getStyleSheets() {
    return styleSheets.getValue();
  }

  /**
   * Sets the current theme. The style sheets will automatically update to the style sheets for the
   * given theme.
   */
  public void setTheme(Theme theme) {
    this.theme.setValue(theme);
  }

  /**
   * Gets the current theme.
   */
  public Theme getTheme() {
    return theme.getValue();
  }

  public Property<Theme> themeProperty() {
    return theme;
  }

}
