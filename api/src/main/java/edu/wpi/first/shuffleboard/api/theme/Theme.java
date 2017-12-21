package edu.wpi.first.shuffleboard.api.theme;

import com.google.common.collect.ImmutableList;

/**
 * A theme is a way of stying the shuffleboard application. Themes specify the location of CSS stylesheets that modify
 * the appearance of the UI.
 */
public class Theme {

  private final String name;
  private final ImmutableList<String> styleSheets;

  /**
   * Creates a new theme with the given name and styled by the given style sheets.
   *
   * @param name        the name of the theme
   * @param styleSheets the locations of the style sheets that the theme uses
   */
  public Theme(String name, String... styleSheets) {
    this.name = name;
    this.styleSheets = ImmutableList.copyOf(styleSheets);
  }

  /**
   * Gets the name of this theme.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the style sheets in this theme.
   */
  public ImmutableList<String> getStyleSheets() {
    return styleSheets;
  }

  @Override
  public String toString() {
    return String.format("Theme(name=%s, styleSheets=%s)", name, styleSheets);
  }

}
