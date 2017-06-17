package edu.wpi.first.shuffleboard.sources.recording;

import java.util.ArrayList;
import java.util.List;

public class Recording {

  private final List<TimestampedData> data = new ArrayList<>();

  public void append(TimestampedData data) {
    this.data.add(data);
    this.data.sort(TimestampedData::compareTo);
  }

  public List<TimestampedData> getData() {
    return data;
  }

}
