package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.Scrubber;
import edu.wpi.first.shuffleboard.sources.recording.Playback;
import edu.wpi.first.shuffleboard.util.FxUtils;

import org.fxmisc.easybind.EasyBind;

import java.util.Optional;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class PlaybackController {

  @FXML
  private Pane root;
  @FXML
  private Button playPauseButton;
  @FXML
  private Scrubber progressScrubber;
  @FXML
  private Label progressLabel;

  private final ImageView playIcon = new ImageView("/edu/wpi/first/shuffleboard/icons/icons8-Play-16.png");
  private final ImageView pauseIcon = new ImageView("/edu/wpi/first/shuffleboard/icons/icons8-Pause-16.png");

  private final Property<Number> progressProperty =
      EasyBind.monadic(Playback.currentPlaybackProperty())
          .selectProperty(Playback::progressProperty);

  private final Property<Boolean> pausedProperty =
      EasyBind.monadic(Playback.currentPlaybackProperty())
          .selectProperty(Playback::pausedProperty);

  @FXML
  private void initialize() {
    pausedProperty.addListener((__, prev, cur) -> {
      if (cur != null) {
        root.setDisable(false);
      }
    });
    progressProperty.addListener(__ -> progressScrubber.setProgressProperty(progressProperty));
    progressScrubber.viewModeProperty().addListener((__, was, is) -> {
      if (is) {
        currentPlayback().ifPresent(Playback::pause);
      }
    });

    progressScrubber.blockIncrementProperty().bind(
        EasyBind.map(Playback.currentPlaybackProperty(),
            playback -> playback == null ? 0.01 : 1.0 / playback.getMaxFrameNum()));

    playPauseButton.graphicProperty().bind(
        EasyBind.map(pausedProperty, paused -> paused == null || paused ? playIcon : pauseIcon));

    progressProperty.addListener((__, prev, cur) -> {
      FxUtils.runOnFxThread(() -> {
        Playback playback = currentPlayback().orElse(null);
        if (cur == null || playback == null) {
          progressLabel.setText("Frame 0 of 0");
        } else {
          progressLabel.setText(String.format("Frame %d of %d",
              (int) (cur.doubleValue() * playback.getMaxFrameNum()), playback.getMaxFrameNum()));
        }
      });
    });
  }

  private static Optional<Playback> currentPlayback() {
    return Optional.ofNullable(Playback.getCurrentPlayback());
  }

  @FXML
  void previousFrame() {
    currentPlayback().ifPresent(Playback::previousFrame);
  }

  @FXML
  void nextFrame() {
    currentPlayback().ifPresent(Playback::nextFrame);
  }

  @FXML
  void togglePlayPause() {
    currentPlayback().ifPresent(playback -> playback.setPaused(!playback.isPaused()));
  }

}
