package edu.wpi.first.shuffleboard.sources.recording;

import java.util.Map;
import java.util.TreeMap;

public class Recording {

  private final Map<String, Map<Long, TimestampedData>> data = new TreeMap<>();

  private Map<Long, TimestampedData> mapForSource(String sourceName) {
    return data.computeIfAbsent(sourceName, __ -> new TreeMap<>());
  }

  public void append(TimestampedData data) {
    mapForSource(data.getSourceId())
        .put(data.getTimestamp(), data);
  }

  public Map<Long, TimestampedData> getDataForSource(String sourceName) {
    return mapForSource(sourceName);
  }

  public Map<String, Map<Long, TimestampedData>> getData() {
    return data;
  }
}
