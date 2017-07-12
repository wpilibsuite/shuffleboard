package edu.wpi.first.shuffleboard.sources.recording;

import edu.wpi.first.shuffleboard.data.ComplexData;
import edu.wpi.first.shuffleboard.sources.CompositeNetworkTableSource;
import edu.wpi.first.shuffleboard.sources.SourceType;
import edu.wpi.first.shuffleboard.sources.Sources;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

  private static final Logger log = Logger.getLogger(Playback.class.getName());

  private final Recording recording;
  private final List<TimestampedData> data;
  private final int numFrames;
  private final int maxFrameNum;
  private Thread autoRunner;
  private volatile boolean started = false;
  private volatile TimestampedData currentFrame;
  private final Object sleepLock = new Object();
  private final BooleanProperty paused = new SimpleBooleanProperty(this, "paused", true);
  private final IntegerProperty frame = new SimpleIntegerProperty(this, "frame", 0);
  private final BooleanProperty looping = new SimpleBooleanProperty(this, "looping", true);

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
    recording = Serialization.loadRecording(logFile);
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
      currentFrame = data.get(newFrame);
      Set<String> remainingSources = new HashSet<>(recording.getSourceIds());
      if (newFrame > lastFrame) {
        // forward
        for (int i = newFrame; i >= lastFrame && !remainingSources.isEmpty(); i--) {
          final TimestampedData data = this.data.get(i);
          if (remainingSources.contains(data.getSourceId())) {
            set(data);
            remainingSources.remove(data.getSourceId());
          }
        }
      } else {
        // backward
        for (int i = newFrame; i <= lastFrame && !remainingSources.isEmpty(); i++) {
          final TimestampedData data = this.data.get(i);
          if (remainingSources.contains(data.getSourceId())) {
            set(data);
            remainingSources.remove(data.getSourceId());
          }
        }
      }
    });
    paused.addListener((__, wasPaused, isPaused) -> {
      if (!isPaused) {
        wakeAutoRunner();
      }
    });
    looping.addListener((__, wasLooping, isLooping) -> {
      if (isLooping) {
        wakeAutoRunner();
      }
    });
  }

  private void wakeAutoRunner() {
    synchronized (sleepLock) {
      sleepLock.notifyAll();
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
    unpause();
    Sources.disconnectAll();
    autoRunner = new Thread(() -> {
      TimestampedData previous;
      TimestampedData current = null;
      int currentFrame = 0;
      while (!Thread.interrupted()) {
        previous = currentFrame == 0 ? null : current;
        current = data.get(currentFrame);
        currentFrame = (getFrame() + 1) % numFrames;

        // Sleep only if we're not paused and the frames are consecutive.
        // If the frames are not consecutive, it means that the frame was manually moved by a user and we would delay
        // when we shouldn't
        if (!isPaused() && previous != null && data.indexOf(current) == data.indexOf(previous) + 1) {
          try {
            Thread.sleep(current.getTimestamp() - previous.getTimestamp());
          } catch (InterruptedException e) {
            log.log(Level.WARNING, "Autorunner thread interrupted between frames", e);
            autoRunner.interrupt();
            return;
          }
        }
        // May have been paused while sleeping, so check after wake to make sure
        if (!isPaused()) {
          setFrame(currentFrame);
        }

        // Halt this thread if playback is paused, or if the end is reached and we're not looping
        if (shouldNotPlayNextFrame()) {
          synchronized (sleepLock) {
            while (shouldNotPlayNextFrame()) {
              try {
                sleepLock.wait();
              } catch (InterruptedException e) {
                log.log(Level.WARNING, "Autorunner thread interrupted while paused", e);
                autoRunner.interrupt();
                return;
              }
            }
          }
        }
      }
      Sources.connectAll();
    }, "PlaybackThread");
    autoRunner.setDaemon(true);
    autoRunner.start();
    started = true;
  }

  private boolean shouldNotPlayNextFrame() {
    return isPaused() || (getFrame() == maxFrameNum && !isLooping());
  }

  private void set(TimestampedData data) {
    final String sourceId = data.getSourceId();
    if (sourceId.startsWith(SourceType.NETWORK_TABLE.getProtocol())) {
      // Update all possible sources for the entry
      // This is a special case because of the treelike structure of network tables
      final String fullKey = SourceType.NETWORK_TABLE.removeProtocol(sourceId);
      List<String> hierarchy = NetworkTableUtils.getHierarchy(fullKey);
      hierarchy.stream()
          .map(SourceType.NETWORK_TABLE::toUri)
          .map(Sources::get)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(source -> {
            if (source instanceof CompositeNetworkTableSource) {
              @SuppressWarnings("unchecked")
              CompositeNetworkTableSource<? extends ComplexData<?>> comp = (CompositeNetworkTableSource) source;
              updateTable(comp, fullKey.substring(comp.getKey().length() + 1), data.getData());
            } else {
              // It's the source just for the key, set it
              source.setData(data.getData());
            }
          });
    } else {
      Sources.get(sourceId)
          .ifPresent(source -> source.setData(data.getData()));
    }
  }

  private <T extends ComplexData<T>> void updateTable(CompositeNetworkTableSource<T> source, String key, Object value) {
    Map<String, Object> map = source.getData().asMap();
    map.put(key, value);
    source.setData(source.getDataType().fromMap(map));
  }

  /**
   * Stops playback.
   */
  public void stop() {
    if (!started) {
      // This playback was never started, so there's no point in stopping it
      return;
    }
    autoRunner.interrupt();
    currentPlayback.setValue(null);
    NetworkTablesJNI.deleteAllEntries();
    Sources.connectAll();
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
