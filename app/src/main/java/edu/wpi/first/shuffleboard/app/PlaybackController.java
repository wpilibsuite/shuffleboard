package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.components.Scrubber;
import edu.wpi.first.shuffleboard.app.sources.recording.Playback;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.FxUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.controlsfx.control.ToggleSwitch;
import org.fxmisc.easybind.EasyBind;

import java.util.Objects;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class PlaybackController {

  @FXML
  private Pane root;
  @FXML
  private Button recordButton;
  @FXML
  private HBox playbackControls;
  @FXML
  private Button playPauseButton;
  @FXML
  private Scrubber progressScrubber;
  @FXML
  private ToggleSwitch loopingSwitch;
  @FXML
  private Label progressLabel;

  private final ImageView recordIcon = new ImageView("/edu/wpi/first/shuffleboard/app/icons/icons8-Record-16.png");
  private final ImageView stopIcon = new ImageView("/edu/wpi/first/shuffleboard/app/icons/icons8-Stop-16.png");
  private final ImageView playIcon = new ImageView("/edu/wpi/first/shuffleboard/app/icons/icons8-Play-16.png");
  private final ImageView pauseIcon = new ImageView("/edu/wpi/first/shuffleboard/app/icons/icons8-Pause-16.png");

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
    playbackControls.disableProperty().bind(
        EasyBind.map(Playback.currentPlaybackProperty(), Objects::isNull));
    recordButton.graphicProperty().bind(
        EasyBind.map(Recorder.getInstance().runningProperty(), running -> running ? stopIcon : recordIcon));
    frameProperty.addListener(__ -> {
      progressScrubber.setProgressProperty(frameProperty);
      progressScrubber.setMax(Playback.getCurrentPlayback().map(Playback::getMaxFrameNum).orElse(0));
    });
    progressScrubber.viewModeProperty().addListener((__, wasViewMode, isViewMode) -> {
      if (isViewMode) {
        Playback.getCurrentPlayback().ifPresent(Playback::pause);
      }
    });

    progressScrubber.setBlockIncrement(1);

    playPauseButton.graphicProperty().bind(
        EasyBind.map(pausedProperty, paused -> paused == null || paused ? playIcon : pauseIcon));

    frameProperty.addListener((__, prev, frame) -> {
      FxUtils.runOnFxThread(() -> {
        Playback playback = Playback.getCurrentPlayback().orElse(null);
        if (frame == null || playback == null) {
          progressLabel.setText("");
        } else {
          TimestampedData first = playback.getRecording().getFirst();
          TimestampedData current = playback.getCurrentFrame();
          if (first == null || current == null) {
            progressLabel.setText("");
          } else {
            String time = msToMinSec(current.getTimestamp() - first.getTimestamp());
            String length = msToMinSec(playback.getRecording().getLength());
            progressLabel.setText(time + " / " + length);
          }
        }
      });
    });

    loopingSwitch.selectedProperty().bindBidirectional(loopingProperty);
  }

  private static String msToMinSec(long ms) {
    long seconds = ms / 1000L;
    return String.format("%d:%02d", seconds / 60, seconds % 60);
  }

  @FXML
  @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "False positive on @FXML method")
  private void previousFrame() {
    Playback.getCurrentPlayback().ifPresent(Playback::previousFrame);
  }

  @FXML
  @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "False positive on @FXML method")
  private void nextFrame() {
    Playback.getCurrentPlayback().ifPresent(Playback::nextFrame);
  }

  @FXML
  private void togglePlayPause() {
    Playback.getCurrentPlayback().ifPresent(playback -> playback.setPaused(!playback.isPaused()));
  }

  @FXML
  private void stopPlayback() {
    Playback.getCurrentPlayback().ifPresent(Playback::stop);
  }

  @FXML
  private void toggleRecord() {
    Recorder recorder = Recorder.getInstance();
    if (recorder.isRunning()) {
      recorder.stop();
    } else {
      recorder.start();
    }
  }

}
