package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.properties.AtomicBooleanProperty;
import edu.wpi.first.shuffleboard.api.properties.AtomicIntegerProperty;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;

import com.google.common.util.concurrent.Futures;

import edu.wpi.first.networktables.NetworkTableInstance;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Handles playback of a recording file. Calling {@link #start()} will start a thread that auto-increments data frames
 * to replay the recorded data at the speed at which it was originally recorded. This thread may be paused and unpaused
 * at at time using the {@link #pausedProperty}, which has the convenience methods {@link #pause()} and
 * {@link #unpause()}. Frames may be set manually using {@link #setFrame}. Frames can be iterated over using
 * {@link #previousFrame()} and {@link #nextFrame()}, which will both pause the auto-incrementing thread.
 */
@SuppressWarnings("PMD.GodClass") // Seriously? It's not _that_ complicated
public final class Playback {

  private final Recording recording;
  private final List<TimestampedData> data;
  private final int numFrames;
  private final int maxFrameNum;
  private volatile boolean started = false;

  // Using atomic properties because they get updated from the executor thread
  private final BooleanProperty paused = new AtomicBooleanProperty(this, "paused", true);
  private final IntegerProperty frame = new AtomicIntegerProperty(this, "frame", 0);
  private final BooleanProperty looping = new AtomicBooleanProperty(this, "looping", true);

  private final ScheduledExecutorService autoRunnerExecutor = ThreadUtils.newDaemonScheduledExecutorService();
  private volatile Future<Integer> nextFrameFuture = Futures.immediateFuture(-1);
  private volatile TimestampedData currentFrame = null;

  private static final Property<Playback> currentPlayback = new SimpleObjectProperty<>(Playback.class, "current", null);

  /**
   * Loads a playback for the given recording file.
   *
   * @throws IOException if the recording file could not be read
   */
  public static Playback load(String recordingFile) throws IOException {
    Playback playback = new Playback(recordingFile);
    getCurrentPlayback().ifPresent(Playback::stop);
    currentPlayback.setValue(playback);
    return playback;
  }

  /**
   * Gets the current playback instance.
   */
  public static Optional<Playback> getCurrentPlayback() {
    return Optional.ofNullable(currentPlayback.getValue());
  }

  public static ReadOnlyProperty<Playback> currentPlaybackProperty() {
    return currentPlayback;
  }

  /**
   * Creates a new playback for the given recording file.
   *
   * @throws IOException if the recording file could not be read
   */
  private Playback(String logFile) throws IOException {
    recording = Serialization.loadRecording(Paths.get(logFile));
    data = recording.getData();
    numFrames = data.size();
    maxFrameNum = numFrames - 1;
    if (numFrames > 0) {
      currentFrame = data.get(0);
    }
    frame.addListener((__, prev, cur) -> {
      if (cur.intValue() < 0 || cur.intValue() > maxFrameNum) {
        throw new IllegalArgumentException(
            String.format("Frame number out of bounds: %s, must be in the range (0, %d)", cur, maxFrameNum));
      }
    });
    frame.addListener((__, prev, cur) -> {
      int lastFrame = prev.intValue();
      int newFrame = cur.intValue();
      if (newFrame - lastFrame != 1 && !(newFrame == 0 && lastFrame == maxFrameNum)) {
        pause();
      }
      currentFrame = data.get(newFrame);
      if (newFrame > lastFrame) {
        forward(lastFrame, newFrame);
      } else {
        backward(lastFrame, newFrame);
      }
    });
    paused.addListener((__, wasPaused, isPaused) -> {
      if (isPaused) {
        nextFrameFuture.cancel(true);
      } else {
        nextFrameFuture = autoRunnerExecutor.submit(() -> moveToNextFrame(getFrame()));
      }
    });
    looping.addListener((__, wasLooping, isLooping) -> {
      if (isLooping && !isPaused()) {
        int frame = getFrame();
        if (frame == maxFrameNum) {
          nextFrameFuture = autoRunnerExecutor.submit(() -> moveToNextFrame(maxFrameNum));
        }
      }
    });
  }

  private void forward(int lastFrame, int newFrame) {
    Set<String> remainingSources = new HashSet<>(recording.getSourceIds());
    for (int i = newFrame; i >= lastFrame && !remainingSources.isEmpty(); i--) {
      final TimestampedData data = this.data.get(i);
      if (remainingSources.contains(data.getSourceId())) {
        set(data);
        remainingSources.remove(data.getSourceId());
      }
    }
  }

  private void backward(int lastFrame, int newFrame) {
    Set<String> remainingSources = new HashSet<>(recording.getSourceIds());
    for (int i = newFrame; i <= lastFrame && !remainingSources.isEmpty(); i++) {
      final TimestampedData data = this.data.get(i);
      if (remainingSources.contains(data.getSourceId())) {
        set(data);
        remainingSources.remove(data.getSourceId());
      }
    }
  }

  /**
   * Starts playback.
   */
  public void start() {
    if (data.isEmpty()) {
      return;
    }
    Recorder.getInstance().stop();
    DashboardMode.setCurrentMode(DashboardMode.PLAYBACK);
    unpause();
    SourceTypes.getDefault().getItems().forEach(SourceType::disconnect);
    nextFrameFuture = autoRunnerExecutor.submit(() -> moveToNextFrame(0));
    started = true;
  }

  /**
   * Moves to the frame after the given one. If the given frame is the last frame, it will move to the first one if
   * {@link #looping} is enabled.
   */
  private int moveToNextFrame(int currentFrameNum) {
    return moveFrame(currentFrameNum, (currentFrameNum + 1) % numFrames);
  }

  /**
   * Moves to the next frame, then schedules itself to be run again after the next frame delay. This pseudo-recursive
   * chaining can be stopped by calling {@code nextFrameFuture.cancel(true)} at any time, usually by setting the
   * {@link #paused} property to {@code false}.
   *
   * <p>If {@code nextFrame} is the last frame and {@link #looping} is enabled, the next run will move to the first
   * frame after no delay. If looping is not enabled, the chain will stop and must be restarted by calling this method
   * again.
   *
   * @param currentFrameNum the current frame
   * @param nextFrameNum    the next frame to be loaded
   *
   * @return the next frame number
   */
  private int moveFrame(int currentFrameNum, int nextFrameNum) {
    if (shouldNotPlayNextFrame()) {
      nextFrameFuture = Futures.immediateFuture(-1);
      return currentFrameNum;
    }
    TimestampedData currentFrame = data.get(currentFrameNum);
    TimestampedData nextFrame = data.get(nextFrameNum);
    setFrame(nextFrameNum);
    boolean consecutive = currentFrameNum == nextFrameNum - 1;
    if (consecutive) {
      // Do a wait to make the data be set at the same rate it was when it was recorded
      long frameTime = nextFrame.getTimestamp() - currentFrame.getTimestamp();
      nextFrameFuture = autoRunnerExecutor.schedule(
          () -> moveToNextFrame(nextFrameNum), frameTime, TimeUnit.MILLISECONDS);
    } else {
      // Frame was changed manually by the user, no sleeps
      nextFrameFuture = autoRunnerExecutor.submit(() -> moveToNextFrame(nextFrameNum));
    }
    return nextFrameNum;
  }

  private boolean shouldNotPlayNextFrame() {
    return isPaused() || (getFrame() == maxFrameNum && !isLooping());
  }

  private void set(TimestampedData data) {
    final String sourceId = data.getSourceId();
    SourceTypes.getDefault()
        .typeForUri(sourceId)
        .read(data);
  }

  /**
   * Stops playback.
   */
  public void stop() {
    if (!started) {
      // This playback was never started, so there's no point in stopping it
      return;
    }
    nextFrameFuture.cancel(true);
    currentPlayback.setValue(null);
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.deleteAllEntries();
    SourceTypes.getDefault().getItems().forEach(SourceType::connect);
    Recorder.getInstance().start();
  }

  public int getNumFrames() {
    return numFrames;
  }

  public int getMaxFrameNum() {
    return maxFrameNum;
  }

  /**
   * Gets the current data frame. Returns null if the loaded recording is empty.
   */
  public TimestampedData getCurrentFrame() {
    return currentFrame;
  }

  /**
   * Gets the recording being played back.
   */
  public Recording getRecording() {
    return recording;
  }

  /**
   * Pauses the auto-incrementing thread.
   */
  public void pause() {
    setPaused(true);
  }

  /**
   * Unpauses the auto-incrementing thread.
   */
  public void unpause() {
    setPaused(false);
  }

  /**
   * Checks if the auto-incrementing thread is currently paused.
   */
  public boolean isPaused() {
    return paused.get();
  }

  public BooleanProperty pausedProperty() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused.set(paused);
  }

  /**
   * Gets the current data frame index.
   */
  public int getFrame() {
    return frame.get();
  }

  public IntegerProperty frameProperty() {
    return frame;
  }

  /**
   * Sets the current data frame index.
   *
   * @throws IllegalArgumentException if the index is negative or greater than the maximum frame index
   */
  public void setFrame(int frame) {
    this.frame.set(frame);
  }

  public boolean isLooping() {
    return looping.get();
  }

  public BooleanProperty loopingProperty() {
    return looping;
  }

  public void setLooping(boolean looping) {
    this.looping.set(looping);
  }

  /**
   * Moves playback to the previous frame, if possible. This pauses the auto-runner.
   */
  public void previousFrame() {
    pause();
    final int frame = getFrame();
    if (frame > 0) {
      setFrame(frame - 1);
    }
  }

  /**
   * Moves playback to the next frame, if possible. This pauses the auto-runner.
   */
  public void nextFrame() {
    pause();
    final int frame = getFrame();
    if (frame < numFrames - 1) {
      setFrame(frame + 1);
    }
  }

}
