package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.ConvertRecordingPaneController;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.google.common.collect.ImmutableList;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

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
  private MonadicBinding<ImmutableList<String>> styleSheets; // NOPMD can be local field

  private void setup() {
    styleSheets = EasyBind.map(AppPreferences.getInstance().themeProperty(), Theme::getStyleSheets);
    stage = new Stage();
    stage.setTitle("Export Recording Files");
    stage.setScene(new Scene(pane.get()));
    stage.setResizable(false);
    FxUtils.bind(stage.getScene().getStylesheets(), styleSheets);
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
