package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import org.fxmisc.easybind.EasyBind;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

@ParametrizedController("DownloadDialogPane.fxml")
public final class DownloadDialogController {

  @FXML
  private Pane root;
  @FXML
  private ProgressBar progressBar;
  @FXML
  private Label label;

  @FXML
  private void initialize() {
    FxUtils.setController(root, this);
    label.textProperty().bind(
        EasyBind.monadic(progressBar.progressProperty())
            .map(Number::doubleValue)
            .map(this::formatAsPercent));
  }

  private String formatAsPercent(double value) {
    return String.format("%.2f%%", value * 100);
  }

  public void setDownloadProgress(double progress) {
    progressBar.setProgress(progress);
  }

}
