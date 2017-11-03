package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializers;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Records data from sources. Each source is responsible for calling {@link #recordCurrentValue} whenever its value
 * changes.
 */
public final class Recorder {

  private static final Logger log = Logger.getLogger(Recorder.class.getName());

  private static final Recorder instance = new Recorder();

  private final BooleanProperty running = new SimpleBooleanProperty(this, "running", false);
  private Instant startTime = null;
  private Recording recording = null;
  private File recordingFile;

  private Recorder() {
    // Save the recording at the start (get the initial values) and the stop
    running.addListener((__, wasRunning, isRunning) -> saveToDisk());
    running.addListener((__, was, is) -> {
      if (is) {
        DashboardMode.setCurrentMode(DashboardMode.RECORDING);
      } else {
        DashboardMode.setCurrentMode(DashboardMode.NORMAL);
      }
    });

    // Save the recording every 2 seconds
    Executors.newSingleThreadScheduledExecutor(ThreadUtils::makeDaemonThread)
        .scheduleAtFixedRate(
            () -> {
              if (isRunning()) {
                saveToDisk();
              }
            }, 0, 2, TimeUnit.SECONDS);
  }

  private void saveToDisk() {
    if (recording == null) {
      // Nothing to save
      return;
    }
    try {
      Path file = Storage.createRecordingFilePath(startTime);
      if (recordingFile == null) {
        recordingFile = file.toFile();
      }
      Serialization.saveRecording(recording, file);
      log.fine("Saved recording to " + file);
    } catch (IOException e) {
      throw new RuntimeException("Could not save the recording", e);
    }
  }

  /**
   * Gets the recorder instance.
   */
  public static Recorder getInstance() {
    return instance;
  }

  /**
   * Starts recording data.
   */
  public void start() {
    startTime = Instant.now();
    recording = new Recording();
    // Record initial conditions
    SourceTypes.getDefault().getItems().stream()
        .map(SourceType::getAvailableSources)
        .forEach(sources -> sources.forEach((id, value) -> {
          DataTypes.getDefault().forJavaType(value.getClass())
              .map(t -> new TimestampedData(id, t, value, 0L))
              .ifPresent(recording::append);
        }));
    setRunning(true);
  }

  /**
   * Stops recording data.
   */
  public void stop() {
    Serializers.cleanUpAll();
    recordingFile = null;
    setRunning(false);
  }

  /**
   * Resets this recorder.
   */
  public void reset() {
    stop();
    start();
  }

  /**
   * Records the current value of the given source.
   */
  public void recordCurrentValue(DataSource<?> source) {
    record(source.getId(), source.getDataType(), source.getData());
  }

  /**
   * Records a data point at the current time.
   *
   * @param id       the ID of the value to record
   * @param dataType the type of the value
   * @param value    the value to record
   */
  public void record(String id, DataType<?> dataType, Object value) {
    if (!isRunning()) {
      return;
    }
    // Store the ID in the common string pool
    // There can easily tens or hundreds of thousands of instances, so storing it in the pool can cut memory
    // use by megabytes per data point
    recording.append(new TimestampedData(id.intern(), dataType, value, timestamp()));
  }

  private long timestamp() {
    return Instant.now().toEpochMilli() - startTime.toEpochMilli();
  }

  public boolean isRunning() {
    return running.get();
  }

  public BooleanProperty runningProperty() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running.set(running);
  }

  public File getRecordingFile() {
    return recordingFile;
  }
}
