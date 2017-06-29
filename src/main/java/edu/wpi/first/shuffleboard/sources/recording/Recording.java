package edu.wpi.first.shuffleboard.sources.recording;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Recording {

  private TimestampedData first;
  private TimestampedData last;
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
    if (first == null || data.getTimestamp() < first.getTimestamp()) {
      first = data;
    }
    if (last == null || data.getTimestamp() > last.getTimestamp()) {
      last = data;
    }
  }

  public List<TimestampedData> getData() {
    return data;
  }

  public List<String> getSourceIds() {
    return sourceIds;
  }

  public TimestampedData getFirst() {
    return first;
  }

  public TimestampedData getLast() {
    return last;
  }

  /**
   * Gets the length of this recording in milliseconds. Recordings wth 0 or 1 data points have a length of 0.
   */
  public long getLength() {
    if (first != null && last != null) {
      return last.getTimestamp() - first.getTimestamp();
    } else {
      return 0;
    }
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
