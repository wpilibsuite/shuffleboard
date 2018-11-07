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
  private final List<Marker> markers = Collections.synchronizedList(new ArrayList<>());
  private final List<String> sourceIds = new ArrayList<>();
  private final List<TimestampedData> dataReadOnly = Collections.unmodifiableList(data);
  private final List<Marker> markersReadOnly = Collections.unmodifiableList(markers);
  private final List<String> sourceIdsReadOnly = Collections.unmodifiableList(sourceIds);

  /**
   * Appends the given data to the end of the data list.
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

  /**
   * Adds a marker to this recording.
   *
   * @param marker the marker to add
   */
  public void addMarker(Marker marker) {
    synchronized (modificationLock) {
      markers.add(marker);
    }
  }

  /**
   * Gets the markers in this recording.
   */
  public List<Marker> getMarkers() {
    synchronized (modificationLock) {
      return markersReadOnly;
    }
  }

  @SuppressWarnings("JavadocMethod")
  public List<TimestampedData> getData() {
    synchronized (modificationLock) {
      return dataReadOnly;
    }
  }

  /**
   * Clears the data and markers to free memory. This should <strong>ONLY</strong> be called by the recording mechanism
   * after the current data is saved to disk.
   */
  @SuppressWarnings("PMD.DefaultPackage") // This should only be used by the Serialization class in the same package
  void clear() {
    synchronized (modificationLock) {
      data.clear();
      markers.clear();
    }
  }

  @SuppressWarnings("JavadocMethod")
  public List<String> getSourceIds() {
    synchronized (modificationLock) {
      return sourceIdsReadOnly;
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

      return this.data.equals(that.data)
          && this.markers.equals(that.markers);
    }
  }

  @Override
  public int hashCode() {
    synchronized (modificationLock) {
      return Objects.hash(data, markers);
    }
  }

  @Override
  public String toString() {
    synchronized (modificationLock) {
      return "Recording(data=" + data + ", markers=" + markers + ")";
    }
  }

}
