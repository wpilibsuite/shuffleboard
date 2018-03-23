package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;

import org.controlsfx.tools.Utils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

public class PreloaderController {

  @FXML
  private Pane root;
  @FXML
  private Label versionLabel;
  @FXML
  private Label stateLabel;
  @FXML
  private ProgressBar progressBar;

  @FXML
  private void initialize() {
    progressBar.setProgress(-1);
    versionLabel.setText(Shuffleboard.getVersion());
    FxUtils.setController(root, this);
  }

  public void setStateText(String text) {
    stateLabel.setText(text + "...");
  }

  public void setProgress(double progress) {
    progressBar.setProgress(Utils.clamp(0, progress, 1));
  }

}
