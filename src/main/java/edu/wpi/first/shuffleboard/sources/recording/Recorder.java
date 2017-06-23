package edu.wpi.first.shuffleboard.sources.recording;

import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.Storage;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Records data from sources. Each source is responsible for calling {@link #recordCurrentValue} whenever its value
 * changes.
 */
public final class Recorder {

  private static final DateTimeFormatter timeFormatter =
      DateTimeFormatter.ofPattern("uuuu-MM-dd_HH:mm:ss", Locale.getDefault());
  private static final Recorder instance = new Recorder();

  private final BooleanProperty running = new SimpleBooleanProperty(this, "running", false);
  private Instant startTime = null;
  private Recording recording = null;

  private Recorder() {
    running.addListener((__, wasRunning, isRunning) -> {
      if (!isRunning) {
        try {
          String file = String.format(Storage.RECORDING_FILE_FORMAT, createTimestamp());
          System.out.println("Saving recording to " + file);
          Serialization.saveRecording(recording, file);
        } catch (IOException e) {
          throw new RuntimeException("Could not save the recording", e);
        }
      }
    });
  }

  private String createTimestamp() {
    return timeFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));
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
    if (!isRunning()) {
      return;
    }
    TimestampedData timestampedData
        = new TimestampedData(source.getId(), source.getDataType(), source.getData(), timestamp());
    recording.append(timestampedData);
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
