package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;

import java.io.IOException;
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

  private Recorder() {
    // Save the recording at the start (get the initial values) and the stop
    running.addListener((__, wasRunning, isRunning) -> saveToDisk());

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
      String file = Storage.createRecordingFilePath(startTime);
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
    setRunning(true);
  }

  /**
   * Stops recording data.
   */
  public void stop() {
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
    recording.append(new TimestampedData(id, dataType, value, timestamp()));
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

}
