package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.components.ShuffleboardDialog;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.MainWindowController;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

/**
 * Dialog for prompting the user to restart the application.
 */
public final class RestartPromptDialog {

  private final LazyInit<Pane> pane
      = LazyInit.of(() -> FXMLLoader.load(MainWindowController.class.getResource("RestartPrompt.fxml")));

  /**
   * Shows the restart prompt.
   *
   * @param primaryWindow the primary application window
   */
  public void show(Window primaryWindow) {
    ShuffleboardDialog dialog = new ShuffleboardDialog(pane.get());
    dialog.setHeaderText("Update Downloaded");
    dialog.initOwner(primaryWindow);
    dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    ButtonType restartNow = new ButtonType("Restart now", ButtonBar.ButtonData.YES);
    ButtonType later = new ButtonType("Later", ButtonBar.ButtonData.NO);
    dialog.getDialogPane().getButtonTypes().addAll(restartNow, later);
    dialog.showAndWait()
        .filter(restartNow::equals)
        .ifPresent(__ -> FxUtils.requestClose(primaryWindow));
  }

}
