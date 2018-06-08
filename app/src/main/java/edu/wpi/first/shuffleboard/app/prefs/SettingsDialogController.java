package edu.wpi.first.shuffleboard.app.prefs;

import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;
import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import org.fxmisc.easybind.EasyBind;

import java.util.Collection;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

@ParametrizedController("SettingsDialog.fxml")
public final class SettingsDialogController {

  @FXML
  private SplitPane root;
  @FXML
  private TreeView<Category> categories;
  @FXML
  private TreeItem<Category> rootItem;
  @FXML
  private StackPane view;

  @FXML
  private void initialize() {
    FxUtils.setController(root, this);

    categories.setCellFactory(v -> {
      TreeCell<Category> cell = new TreeCell<>();
      cell.setPrefWidth(1);
      cell.textProperty().bind(EasyBind.monadic(cell.itemProperty()).map(Category::getName));
      return cell;
    });

    categories.getSelectionModel().selectedItemProperty().addListener((__, old, item) -> {
      Category category = item.getValue();
      setViewForCategory(category);
    });

    rootItem.getChildren().addListener((InvalidationListener) __ -> {
      if (!rootItem.getChildren().isEmpty()) {
        categories.getSelectionModel().select(0);
        setViewForCategory(rootItem.getChildren().get(0).getValue());
      }
    });

    Platform.runLater(() -> root.setDividerPositions(0));
  }

  private void setViewForCategory(Category category) {
    if (category.getGroups().isEmpty()) {
      view.getChildren().setAll(new Label("No settings for " + category.getName()));
    } else {
      view.getChildren().setAll(new ExtendedPropertySheet(category));
    }
  }

  /**
   * Sets the root settings categories to display in the settings view. Subcategories will be displayed as subtrees
   * underneath their parent category.
   *
   * @param rootCategories the root categories to display in the settings view
   */
  public void setRootCategories(Collection<Category> rootCategories) {
    rootItem.getChildren().clear();
    for (Category rootCategory : rootCategories) {
      TreeItem<Category> item = new TreeItem<>(rootCategory);
      addSubcategories(item);
      rootItem.getChildren().add(item);
    }
  }

  private void addSubcategories(TreeItem<Category> rootItem) {
    rootItem.getValue().getSubcategories()
        .forEach(category -> {
          rootItem.setExpanded(true);
          TreeItem<Category> item = new TreeItem<>(category);
          addSubcategories(item);
          rootItem.getChildren().add(item);
        });
  }

  /**
   * Applies the user-made changes to the settings. Most changes affect their respective properties immediately,
   * but flushable properties need to be manually updated.
   */
  public void applySettings() {
    applySettings(rootItem);
  }

  private void applySettings(TreeItem<Category> item) {
    if (item.getValue() != null) {
      item.getValue().getGroups()
          .stream()
          .map(Group::getSettings)
          .flatMap(Collection::stream)
          .map(Setting::getProperty)
          .flatMap(TypeUtils.castStream(FlushableProperty.class))
          .filter(FlushableProperty::isChanged)
          .forEach(FlushableProperty::flush);
    }
    item.getChildren().forEach(this::applySettings);
  }

}
