package edu.wpi.first.shuffleboard.sources.recording;

import com.google.gson.Gson;

import edu.wpi.first.shuffleboard.sources.DataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Recorder {

  private static final Gson gson = new Gson();

  private static Recorder instance = null;

  private BooleanProperty running = new SimpleBooleanProperty(this, "running", false);
  private Instant startTime = null;
  private Recording recording = null;

  private Recorder() {
    running.addListener((__, wasRunning, isRunning) -> {
      if (!isRunning) {
        String json = gson.toJson(recording);
        try {
          Files.write(Paths.get("/home", "sam", "dashboard_log.log"), json.getBytes());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static Recorder getInstance() {
    if (instance == null) {
      instance = new Recorder();
    }
    return instance;
  }

  public void start() {
    startTime = Instant.now();
    recording = new Recording();
    setRunning(true);
  }

  public void stop() {
    setRunning(false);
  }

  public void reset() {
    stop();
    start();
  }

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
