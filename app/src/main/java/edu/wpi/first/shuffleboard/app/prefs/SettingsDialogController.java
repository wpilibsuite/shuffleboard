package edu.wpi.first.shuffleboard.app.prefs;

import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import java.util.Collection;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

@ParametrizedController("SettingsDialog.fxml")
public final class SettingsDialogController {

  @FXML
  private SplitPane root;
  @FXML
  private ListView<Category> categories;
  @FXML
  private StackPane view;

  @FXML
  private void initialize() {
    FxUtils.setController(root, this);

    root.getStyleClass().add("settings-pane");
    categories.setCellFactory(v -> {
      TextFieldListCell<Category> cell = new TextFieldListCell<>();
      cell.setConverter(new StringConverter<Category>() {
        @Override
        public String toString(Category category) {
          return category.getName();
        }

        @Override
        public Category fromString(String string) {
          throw new UnsupportedOperationException();
        }
      });
      return cell;
    });

    categories.getSelectionModel().selectedIndexProperty().addListener((__, old, index) -> {
      if (index.intValue() < 0) {
        index = 0;
      }
      view.getChildren().setAll(categories.getItems().get(index.intValue()).createPropertySheet());
    });

    categories.getItems().addListener((InvalidationListener) __ -> {
      if (!categories.getItems().isEmpty()) {
        Platform.runLater(() -> {
          categories.getSelectionModel().select(0);
        });
      }
    });

    Platform.runLater(() -> root.setDividerPositions(0));
  }

  public void setCategories(Collection<Category> categories) {
    this.categories.getItems().setAll(categories);
  }

  /**
   * Applies the user-made changes to the settings. Most changes affect their respective properties immediately,
   * but flushable properties need to be manually updated.
   */
  public void applySettings() {
    categories.getItems().stream()
        .map(Category::getGroups)
        .flatMap(Collection::stream)
        .map(Group::getSettings)
        .flatMap(Collection::stream)
        .map(Setting::getProperty)
        .flatMap(TypeUtils.castStream(FlushableProperty.class))
        .filter(FlushableProperty::isChanged)
        .forEach(FlushableProperty::flush);
  }

}
