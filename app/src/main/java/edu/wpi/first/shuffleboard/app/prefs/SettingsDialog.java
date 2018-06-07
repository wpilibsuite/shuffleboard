package edu.wpi.first.shuffleboard.app.prefs;

import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.util.FxUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

/**
 * A dialog for editing {@link edu.wpi.first.shuffleboard.api.prefs.Setting settings}. Each {@link Category} gets an
 * entry in a list on the left-hand side of the dialog. Selecting a category will display its property sheet in the
 * main right-hand view. The first category in the list will be selected by default.
 */
public final class SettingsDialog extends Dialog<Boolean> {

  /**
   * Creates a new settings dialog.
   *
   * @param categories the categories for settings to be edited in the dialog
   */
  public SettingsDialog(Category... categories) {
    this(Arrays.asList(categories));
  }

  /**
   * Creates a new settings dialog.
   *
   * @param categories the categories for settings to be edited in the dialog
   */
  public SettingsDialog(Collection<Category> categories) {
    SettingsDialogController controller;
    try {
      Node root = FxUtils.load(SettingsDialogController.class);
      controller = FxUtils.getController(root);
      controller.setRootCategories(categories);

      getDialogPane().setContent(root);
    } catch (IOException e) {
      throw new AssertionError("Could not load FXML for settings dialog", e);
    }

    initModality(Modality.APPLICATION_MODAL);
    initStyle(StageStyle.UTILITY);
    getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    setResizable(true);

    setResultConverter(button -> !button.getButtonData().isCancelButton());
    setOnCloseRequest(e -> {
      if (getResult() != null && getResult()) {
        controller.applySettings();
      }
    });

    Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds(); // TODO use the screen the app is displayed on
    getDialogPane().setPrefSize(
        Math.max(600, visualBounds.getWidth() / 2),
        Math.max(400, visualBounds.getHeight() / 2)
    );
  }

}
