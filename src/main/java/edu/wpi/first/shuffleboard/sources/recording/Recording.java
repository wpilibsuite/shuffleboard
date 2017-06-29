package edu.wpi.first.shuffleboard.sources.recording;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Recording {

  private final List<TimestampedData> data = Collections.synchronizedList(new ArrayList<>());
  private final List<String> sourceIds = new ArrayList<>();

  /**
   * Appends the given data to the end of the data list. Note that this does not
   *
   * @param data the data to append
   */
  public void append(TimestampedData data) {
    this.data.add(data);
    if (!sourceIds.contains(data.getSourceId())) {
      sourceIds.add(data.getSourceId());
    }
  }

  public List<TimestampedData> getData() {
    return data;
  }

  public List<String> getSourceIds() {
    return sourceIds;
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
