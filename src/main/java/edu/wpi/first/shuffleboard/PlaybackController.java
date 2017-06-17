package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.sources.recording.Playback;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class PlaybackController {

  @FXML
  private Pane root;
  @FXML
  private Button playPauseButton;
  @FXML
  private Slider progressSlider;

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
    progressSlider.valueProperty().bindBidirectional(progressProperty);
    playPauseButton.graphicProperty().bind(
        EasyBind.map(pausedProperty, paused -> paused == null || paused ? playIcon : pauseIcon));
  }

  private static Playback playback() {
    return Playback.getCurrentPlayback();
  }

  private static boolean hasPlayback() {
    return playback() != null;
  }

  @FXML
  private void previousFrame() {
    if (!hasPlayback()) {
      return;
    }
    playback().previousFrame();
  }

  @FXML
  private void nextFrame() {
    if (!hasPlayback()) {
      return;
    }
    playback().nextFrame();
  }

  @FXML
  private void togglePlayPause() {
    if (!hasPlayback()) {
      return;
    }
    playback().setPaused(!playback().isPaused());
  }

}
