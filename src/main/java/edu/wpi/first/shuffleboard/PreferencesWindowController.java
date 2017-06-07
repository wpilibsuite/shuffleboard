package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.theme.Theme;
import edu.wpi.first.shuffleboard.theme.ThemeManager;
import edu.wpi.first.shuffleboard.theme.DefaultThemes;
import edu.wpi.first.shuffleboard.util.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class PreferencesWindowController {

  @FXML
  private Pane root;
  @FXML
  private ComboBox<Theme> themeBox;

  @FXML
  private void initialize() {
    FxUtils.bind(root.getStylesheets(), ThemeManager.styleSheetsProperty());
    themeBox.setItems(FXCollections.observableArrayList(DefaultThemes.LIGHT, DefaultThemes.DARK));
    themeBox.setConverter(new ThemeStringConverter());
    themeBox.getSelectionModel().select(ThemeManager.getTheme());
    themeBox.getSelectionModel()
            .selectedItemProperty()
            .addListener((__, oldTheme, newTheme) -> ThemeManager.setTheme(newTheme));
  }

  public static class ThemeStringConverter extends StringConverter<Theme> {

    @Override
    public String toString(Theme object) {
      return object.getName();
    }

    @Override
    public Theme fromString(String string) {
      switch (string) {
        case "Light":
          return DefaultThemes.LIGHT;
        case "Dark":
          return DefaultThemes.DARK;
        default:
          return DefaultThemes.LIGHT;
      }
    }
  }
}
