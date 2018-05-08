package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;

import org.controlsfx.tools.Utils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class PreloaderController {

  private static final Color primaryColor = Color.rgb(38, 38, 247); // YInMn blue
  private static final Color secondaryColor = Color.rgb(64, 64, 255);

  private static final double HEXAGON_RADIUS = 10;

  @FXML
  private Pane root;
  @FXML
  private Pane backgroundContainer;
  @FXML
  private Label versionLabel;
  @FXML
  private Label stateLabel;
  @FXML
  private ProgressBar progressBar;

  @FXML
  private void initialize() {
    // Bring the hexagons to the top edge to avoid weird blank spots
    backgroundContainer.setTranslateY(-HEXAGON_RADIUS);

    progressBar.setProgress(-1);
    versionLabel.setText(Shuffleboard.getVersion());

    // Hexagon grid background
    HexagonGrid hexagonGrid = new HexagonGrid(
        (int) (root.getPrefWidth() / HEXAGON_RADIUS),
        (int) (root.getPrefHeight() / HEXAGON_RADIUS),
        HEXAGON_RADIUS,
        0);

    // Set colors randomly
    hexagonGrid.hexagons().forEach(hexagon -> {
      hexagon.setFill(primaryColor.interpolate(secondaryColor, Math.random()));
    });
    backgroundContainer.getChildren().setAll(hexagonGrid);

    FxUtils.setController(root, this);
  }

  public void setStateText(String text) {
    stateLabel.setText(text);
  }

  public void setProgress(double progress) {
    progressBar.setProgress(Utils.clamp(0, progress, 1));
  }

}
