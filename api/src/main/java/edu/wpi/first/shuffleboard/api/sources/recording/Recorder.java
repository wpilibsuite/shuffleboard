package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.properties.AtomicBooleanProperty;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializer;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.Serializers;
import edu.wpi.first.shuffleboard.api.util.ShutdownHooks;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;

import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Records data from sources. Each source is responsible for calling {@link #recordCurrentValue} whenever its value
 * changes.
 */
public final class Recorder {

  private static final Logger log = Logger.getLogger(Recorder.class.getName());

  public static final String DEFAULT_RECORDING_FILE_NAME_FORMAT = "recording-${time}";
  private static final Recorder instance = new Recorder();

  private final BooleanProperty running = new AtomicBooleanProperty(this, "running", false);
  private final StringProperty fileNameFormat =
      new SimpleStringProperty(this, "fileNameFormat", DEFAULT_RECORDING_FILE_NAME_FORMAT);
  private String currentFileNameFormat = DEFAULT_RECORDING_FILE_NAME_FORMAT; // NOPMD - PMD can't handle lambdas
  private Instant startTime = null;
  private Recording recording = null;
  private File recordingFile;

  private final Object startStopLock = new Object();
  private boolean firstSave = true;

  private final boolean enableDiskWrites;

  private Recorder(boolean enableDiskWrites) {
    this.enableDiskWrites = enableDiskWrites;
    // Save the recording at the start (get the initial values) and the stop
    running.addListener((__, wasRunning, isRunning) -> {
      try {
        currentFileNameFormat = getFileNameFormat();
        saveToDisk();
      } catch (IOException e) {
        log.log(Level.WARNING, "Could not save to disk", e);
      }
    });
    running.addListener((__, was, is) -> {
      if (is) {
        DashboardMode.setCurrentMode(DashboardMode.RECORDING);
      } else {
        DashboardMode.setCurrentMode(DashboardMode.NORMAL);
      }
    });

    // Save the recording every 2 seconds
    if (enableDiskWrites) {
      Executors.newSingleThreadScheduledExecutor(ThreadUtils::makeDaemonThread)
          .scheduleAtFixedRate(
              () -> {
                if (isRunning()) {
                  try {
                    saveToDisk();
                  } catch (Exception e) {
                    log.log(Level.WARNING, "Could not save recording", e);
                  }
                }
              }, 0, 2, TimeUnit.SECONDS);
    }
    ShutdownHooks.addHook(this::stop);
  }

  private Recorder() {
    this(true);
  }

  private void saveToDisk() throws IOException {
    if (recording == null || !enableDiskWrites) {
      // Nothing to save
      return;
    }
    Path file = Storage.createRecordingFilePath(startTime, currentFileNameFormat);
    if (recordingFile == null) {
      recordingFile = file.toFile();
    }
    synchronized (startStopLock) {
      if (firstSave) {
        Serialization.saveRecording(recording, file);
        firstSave = false;
      } else {
        Serialization.updateRecordingSave(recording, file);
      }
      Serializers.getAdapters().forEach(Serializer::flush);
    }
    log.fine("Saved recording to " + file);
  }

  /**
   * Gets the recorder instance.
   */
  public static Recorder getInstance() {
    return instance;
  }

  /**
   * Creates a new Recorder instance that does not write anything to disk.
   */
  public static Recorder createDummyInstance() {
    return new Recorder(false);
  }

  /**
   * Starts recording data.
   */
  public void start() {
    synchronized (startStopLock) {
      startTime = Instant.now();
      firstSave = true;
      recording = new Recording();
      // Record initial conditions
      SourceTypes.getDefault().getItems().stream()
          .map(SourceType::getAvailableSources)
          .forEach(sources -> sources.forEach((id, value) -> {
            DataTypes.getDefault().forJavaType(value.getClass())
                .map(t -> new TimestampedData(id, t, value, 0L))
                .ifPresent(recording::append);
          }));
    }
    setRunning(true);
  }

  /**
   * Stops recording data.
   */
  public void stop() {
    try {
      saveToDisk();
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not save last data to disk", e);
    }
    synchronized (startStopLock) {
      setRunning(false);
      recordingFile = null;
      Serializers.cleanUpAll();
    }
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
    recording.append(new TimestampedData(id.intern(), dataType, value, timestamp()));
  }

  /**
   * Adds a marker to the recording at the current time.
   *
   * @param name        the name of the marker
   * @param description a description of the marked event
   * @param importance  the importance of the marker
   */
  public void addMarker(String name, String description, MarkerImportance importance) {
    if (!isRunning()) {
      return;
    }
    recording.addMarker(new Marker(name, description, importance, timestamp()));
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

  public String getFileNameFormat() {
    return fileNameFormat.get();
  }

  public StringProperty fileNameFormatProperty() {
    return fileNameFormat;
  }

  public void setFileNameFormat(String fileNameFormat) {
    this.fileNameFormat.set(fileNameFormat);
  }

  /**
   * Gets the recording being recorded to. This method should only be used for tests to make sure the recording is
   * being used properly.
   */
  @VisibleForTesting
  public Recording getRecording() {
    synchronized (startStopLock) {
      return recording;
    }
  }
}
