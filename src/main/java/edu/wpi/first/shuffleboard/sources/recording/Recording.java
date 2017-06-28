package edu.wpi.first.shuffleboard.sources.recording;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Recording {

  private final List<TimestampedData> data = Collections.synchronizedList(new ArrayList<>());

  public void append(TimestampedData data) {
    this.data.add(data);
    this.data.sort(TimestampedData::compareTo);
  }

  public List<TimestampedData> getData() {
    return data;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Recording that = (Recording) obj;

    return this.data.equals(that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }

  @Override
  public String toString() {
    return "Recording(data=" + data + ")";
  }

}
