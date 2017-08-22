package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.app.sources.NetworkTableSourceType;
import edu.wpi.first.shuffleboard.app.util.Storage;
import edu.wpi.first.shuffleboard.app.util.ThreadUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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

  private static final DateTimeFormatter timeFormatter =
      DateTimeFormatter.ofPattern("uuuu-MM-dd_HH:mm:ss", Locale.getDefault());
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
      String file = String.format(Storage.RECORDING_FILE_FORMAT, createTimestamp());
      Serialization.saveRecording(recording, file);
      log.fine("Saved recording to " + file);
    } catch (IOException e) {
      throw new RuntimeException("Could not save the recording", e);
    }
  }

  private String createTimestamp() {
    return timeFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));
  }

  static {
    // Automatically capture and record changes in network tables
    // This is done here because each key under N subtables would have N+1 copies
    // in the recording (eg "/a/b/c" has 2 tables and 3 copies: "/a", "/a/b", and "/a/b/c")
    // This significantly reduces the size of recording files.
    NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
      getInstance().record(NetworkTableSourceType.INSTANCE.toUri(key), DataType.forJavaType(value.getClass()), value);
    }, 0xFF);
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
