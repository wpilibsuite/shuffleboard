package edu.wpi.first.shuffleboard.api.sources.recording;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Recording {

  private final Object modificationLock = new Object();
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
    synchronized (modificationLock) {
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
  }

  @SuppressWarnings("JavadocMethod")
  public List<TimestampedData> getData() {
    synchronized (modificationLock) {
      return data;
    }
  }

  @SuppressWarnings("JavadocMethod")
  public List<String> getSourceIds() {
    synchronized (modificationLock) {
      return sourceIds;
    }
  }

  @SuppressWarnings("JavadocMethod")
  public TimestampedData getFirst() {
    synchronized (modificationLock) {
      return first;
    }
  }

  @SuppressWarnings("JavadocMethod")
  public TimestampedData getLast() {
    synchronized (modificationLock) {
      return last;
    }
  }

  /**
   * Gets the length of this recording in milliseconds. Recordings wth 0 or 1 data points have a length of 0.
   */
  public long getLength() {
    synchronized (modificationLock) {
      if (first == null || last == null) {
        return 0;
      } else {
        return last.getTimestamp() - first.getTimestamp();
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    synchronized (modificationLock) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      Recording that = (Recording) obj;

      return this.data.equals(that.data);
    }
  }

  @Override
  public int hashCode() {
    synchronized (modificationLock) {
      return Objects.hash(data);
    }
  }

  @Override
  public String toString() {
    synchronized (modificationLock) {
      return "Recording(data=" + data + ")";
    }
  }

}
