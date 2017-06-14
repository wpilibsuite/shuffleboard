package edu.wpi.first.shuffleboard.sources.recording;

import com.google.gson.Gson;

import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.Sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.stream.Collectors;

public class Playback {

  private Recording recording;
  private Deque<TimestampedData> dataQueue;
  private Thread thread;

  public Playback(String logFile) throws IOException {
    recording = load(logFile);
    dataQueue =
        recording.getData()
            .entrySet()
            .stream()
            .flatMap(e -> e.getValue().values().stream())
            .sorted(Comparator.comparingLong(TimestampedData::getTimestamp)
                .thenComparing(TimestampedData::getSourceId))
            .collect(Collectors.toCollection(ArrayDeque::new));
  }

  private Recording load(String logFile) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(logFile));
    return new Gson().fromJson(new String(bytes), Recording.class);
  }

  public void start() {
    Sources.disconnectAll();
    thread = new Thread(() -> {
      TimestampedData previous;
      TimestampedData current = null;
      while (!Thread.interrupted() && !dataQueue.isEmpty()) {
        previous = current;
        current = dataQueue.pop();
        if (previous != null) {
          try {
            Thread.sleep(current.getTimestamp() - previous.getTimestamp());
          } catch (InterruptedException e) {
            e.printStackTrace();
            thread.interrupt();
          }
        }
        Sources.getOrDefault(current.getSourceId(), DataSource.none()).setData(current.getData());
      }
      Sources.connectAll();
    }, "DataPlaybackThread");
    thread.setDaemon(true);
    thread.start();
  }

  public void stop() {
    thread.interrupt();
    Sources.connectAll();
  }

}
