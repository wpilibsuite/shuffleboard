package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.DownloadDialogController;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Dialog for displaying download progress of an update.
 */
public final class UpdateDownloadDialog {

  private boolean initialized = false;
  private final LazyInit<Pane> downloadPane = LazyInit.of(() -> FxUtils.load(DownloadDialogController.class));
  private Stage stage;

  private void setup() {
    stage = new Stage();
    Pane pane = downloadPane.get();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.setScene(new Scene(pane));
    stage.getScene().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    pane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.focusedProperty().addListener((__, was, is) -> {
      if (!is) {
        stage.close();
      }
    });
    initialized = true;
  }

  /**
   * Shows the dialog.
   */
  public void show() {
    if (!initialized) {
      setup();
    }
    stage.show();
  }

  /**
   * Closes the dialog.
   */
  public void close() {
    stage.close();
  }

  /**
   * Checks if the dialog is currently visible.
   *
   * @return true if the dialog is visible, false if not
   */
  public boolean isShowing() {
    return stage != null && stage.isShowing();
  }

  /**
   * Sets the current download progress.
   * @param progress the current download progress, in the range [0, 1].
   */
  public void setDownloadProgress(double progress) {
    DownloadDialogController controller = FxUtils.getController(downloadPane.get());
    controller.setDownloadProgress(progress);
  }

}
