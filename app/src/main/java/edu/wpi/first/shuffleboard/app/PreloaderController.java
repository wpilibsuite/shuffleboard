package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;

import org.controlsfx.tools.Utils;

import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static org.controlsfx.tools.Utils.clamp;

public class PreloaderController {

  private static final Color primaryColor = Color.rgb(39, 39, 247);
  private static final Color secondaryColor = Color.rgb(64, 64, 255);

  // Animation timings
  private static final double minTime = 0.75;
  private static final double maxTime = 1.25;

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

    // Animated hexagon grid background
    Random random = new Random(System.currentTimeMillis() ^ (System.currentTimeMillis() >> 16));
    HexagonGrid hexagonGrid = new HexagonGrid(
        (int) (root.getPrefWidth() / HEXAGON_RADIUS),
        (int) (root.getPrefHeight() / HEXAGON_RADIUS),
        HEXAGON_RADIUS,
        0);
    // animate the hexagons
    hexagonGrid.hexagons()
        .stream()
        .map(h -> new FillTransition(
            Duration.seconds(
                clamp(0, random.nextGaussian() + 1, 2) * (maxTime - minTime) + minTime),
            h, primaryColor, secondaryColor))
        .peek(t -> t.setCycleCount(Animation.INDEFINITE))
        .peek(t -> t.setAutoReverse(true))
        .forEach(t -> t.playFrom(Duration.seconds(random.nextDouble() * maxTime)));
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
