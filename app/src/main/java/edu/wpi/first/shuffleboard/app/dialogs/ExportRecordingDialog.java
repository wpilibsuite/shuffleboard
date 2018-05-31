package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.ConvertRecordingPaneController;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for allowing users to convert data recordings from the binary format to other formats.
 */
public final class ExportRecordingDialog {

  private boolean initialized = false;
  private final LazyInit<Pane> pane = LazyInit.of(() -> FxUtils.load(ConvertRecordingPaneController.class));
  private Stage stage;

  private void setup() {
    stage = new Stage();
    stage.setTitle("Export Recording Files");
    stage.setScene(new Scene(pane.get()));
    stage.setResizable(false);
    pane.get().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    stage.initModality(Modality.APPLICATION_MODAL);
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

}
