package com.example.theme;

import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.theme.Theme;

import java.util.List;

@Description(
    group = "com.example",
    name = "ThemeExample",
    version = "2019.1.1",
    summary = "An example plugin that provides a custom CSS theme for Shuffleboard"
)
public final class ThemeExamplePlugin extends Plugin {

  private final Theme customTheme = new Theme(getClass(), "Example Theme", "custom-shuffleboard-theme.css");

  @Override
  public List<Theme> getThemes() {
    return List.of(customTheme);
  }
}
