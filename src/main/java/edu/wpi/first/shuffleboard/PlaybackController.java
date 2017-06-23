package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.Scrubber;
import edu.wpi.first.shuffleboard.sources.recording.Playback;
import edu.wpi.first.shuffleboard.util.FxUtils;

import org.controlsfx.control.ToggleSwitch;
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
  private ToggleSwitch loopingSwitch;
  @FXML
  private Label progressLabel;

  private final ImageView playIcon = new ImageView("/edu/wpi/first/shuffleboard/icons/icons8-Play-16.png");
  private final ImageView pauseIcon = new ImageView("/edu/wpi/first/shuffleboard/icons/icons8-Pause-16.png");

  private final Property<Number> frameProperty =
      EasyBind.monadic(Playback.currentPlaybackProperty())
          .selectProperty(Playback::frameProperty);

  private final Property<Boolean> pausedProperty =
      EasyBind.monadic(Playback.currentPlaybackProperty())
          .selectProperty(Playback::pausedProperty);

  private final Property<Boolean> loopingProperty =
      EasyBind.monadic(Playback.currentPlaybackProperty())
          .selectProperty(Playback::loopingProperty);

  @FXML
  private void initialize() {
    Playback.currentPlaybackProperty().addListener((__, prev, cur) -> {
      if (cur != null) {
        root.setDisable(false);
      }
    });
    frameProperty.addListener(__ -> {
      progressScrubber.setProgressProperty(frameProperty);
      progressScrubber.setMax(Playback.getCurrentPlayback().getMaxFrameNum());
    });
    progressScrubber.viewModeProperty().addListener((__, wasViewMode, isViewMode) -> {
      if (isViewMode) {
        currentPlayback().ifPresent(Playback::pause);
      }
    });

    progressScrubber.setBlockIncrement(1);

    playPauseButton.graphicProperty().bind(
        EasyBind.map(pausedProperty, paused -> paused == null || paused ? playIcon : pauseIcon));

    frameProperty.addListener((__, prev, currentProgress) -> {
      FxUtils.runOnFxThread(() -> {
        Playback playback = currentPlayback().orElse(null);
        if (currentProgress == null || playback == null) {
          progressLabel.setText("Frame 0 of 0");
        } else {
          progressLabel.setText(String.format("Frame %d of %d", currentProgress.intValue(), playback.getMaxFrameNum()));
        }
      });
    });

    loopingSwitch.selectedProperty().bindBidirectional(loopingProperty);
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
