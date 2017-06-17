package edu.wpi.first.shuffleboard.sources.recording;

import com.google.gson.Gson;

import edu.wpi.first.shuffleboard.sources.DataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Records data from sources. Each source is responsible for calling {@link #recordCurrentValue} whenever its value
 * changes.
 */
public final class Recorder {

  private static final Gson gson = new Gson();

  private static final Recorder instance = new Recorder();

  private final BooleanProperty running = new SimpleBooleanProperty(this, "running", false);
  private Instant startTime = null;
  private Recording recording = null;

  private Recorder() {
    running.addListener((__, wasRunning, isRunning) -> {
      if (!isRunning) {
        String json = gson.toJson(recording);
        try {
          Files.write(Paths.get("dashboard_log.log"), json.getBytes());
        } catch (IOException e) {
          throw new RuntimeException("Could not write to the log file", e);
        }
      }
    });
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
