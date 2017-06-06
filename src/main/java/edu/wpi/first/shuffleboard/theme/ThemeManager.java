package edu.wpi.first.shuffleboard.theme;

import com.google.common.collect.ImmutableList;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Manages the CSS themes for the application.
 */
public final class ThemeManager {

  private static final Property<Theme> theme
      = new SimpleObjectProperty<>(ThemeManager.class, "theme", DefaultThemes.LIGHT);

  private static final Property<ImmutableList<String>> styleSheets
      = new SimpleObjectProperty<>(ThemeManager.class, "styleSheets", ImmutableList.of());

  static {
    styleSheets.bind(Bindings.createObjectBinding(() -> getTheme().getStyleSheets(), theme));
  }

  private ThemeManager() {
  }

  public static ReadOnlyProperty<ImmutableList<String>> styleSheetsProperty() {
    return styleSheets;
  }

  /**
   * Gets the current application stylesheets.
   */
  public static ImmutableList<String> getStyleSheets() {
    return styleSheets.getValue();
  }

  /**
   * Sets the current theme. The style sheets will automatically update to the style sheets for the
   * given theme.
   */
  public static void setTheme(Theme theme) {
    ThemeManager.theme.setValue(theme);
  }

  /**
   * Gets the current theme.
   */
  public static Theme getTheme() {
    return theme.getValue();
  }

  public static Property<Theme> themeProperty() {
    return theme;
  }

}
