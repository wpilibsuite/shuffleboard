package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.PluginPaneController;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for plugins controller.
 */
public final class PluginDialog {

  private boolean initialized = false;

  // Lazy init to avoid unnecessary loading if the dialog is never used
  private final LazyInit<Pane> pane = LazyInit.of(() -> FxUtils.load(PluginPaneController.class));
  private Stage stage;

  private void setup() {
    initialized = true;
    stage = new Stage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.setScene(new Scene(pane.get()));
    stage.sizeToScene();
    stage.setMinWidth(675);
    stage.setMinHeight(325);
    stage.setTitle("Loaded Plugins");
    pane.get().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
  }

  /**
   * Shows the plugin dialog.
   */
  public void show() {
    if (!initialized) {
      setup();
    }
    stage.show();
  }

}
