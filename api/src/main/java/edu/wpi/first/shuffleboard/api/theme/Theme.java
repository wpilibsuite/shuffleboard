package edu.wpi.first.shuffleboard.api.theme;

import com.google.common.collect.ImmutableList;

import java.net.URL;
import java.util.stream.Stream;

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
   * <p><strong>Themes defined in plugins MUST NOT use this constructor.</strong> Themes defined in plugin jars
   * will be unable to find their stylesheets, since those stylesheets are in a JAR somewhere that shuffleboard will
   * not necessarily be aware of.
   *
   * @param name        the name of the theme
   * @param styleSheets the locations of the style sheets that the theme uses
   */
  public Theme(String name, String... styleSheets) {
    this.name = name;
    this.styleSheets = ImmutableList.copyOf(styleSheets);
  }

  /**
   * Creates a new theme with the given name and styled by the given style sheets. This is intended for use for
   * themes defined in plugins.
   *
   * @param localClass  a class defined in the plugin. This parameter allows Shuffleboard to be able to locate the
   *                    stylesheets within the plugin JAR.
   * @param name        the name of the theme
   * @param styleSheets the locations of the style sheets that the theme uses
   */
  public Theme(Class<?> localClass, String name, String... styleSheets) {
    this.name = name;
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    Stream.of(styleSheets)
        .map(localClass.getClassLoader()::getResource)
        .map(URL::toExternalForm)
        .forEach(builder::add);
    this.styleSheets = builder.build();
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
