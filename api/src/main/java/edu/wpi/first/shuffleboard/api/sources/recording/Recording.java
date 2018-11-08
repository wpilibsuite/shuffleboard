package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.util.ThreadUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Recording {

  private TimestampedData first;
  private TimestampedData last;
  private final List<TimestampedData> data = Collections.synchronizedList(new ArrayList<>());
  private final List<Marker> markers = Collections.synchronizedList(new ArrayList<>());
  private final List<String> sourceIds = new ArrayList<>();
  private final List<TimestampedData> dataReadOnly = Collections.unmodifiableList(data);
  private final List<Marker> markersReadOnly = Collections.unmodifiableList(markers);
  private final List<String> sourceIdsReadOnly = Collections.unmodifiableList(sourceIds);

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Appends the given data to the end of the data list.
   *
   * @param data the data to append
   */
  public void append(TimestampedData data) {
    try {
      lock.writeLock().lock();
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
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Adds a marker to this recording.
   *
   * @param marker the marker to add
   */
  public void addMarker(Marker marker) {
    ThreadUtils.withLock(lock.writeLock(), () -> markers.add(marker));
  }

  /**
   * Gets the markers in this recording. Note: if you need to get the data objects and markers at the same time,
   * use {@link #copyData copyData()} to avoid locking issues (the individual methods each lock, but the lists may
   * change between method calls).
   *
   * <p>In live recordings, this list will not be exhaustive. Markers are removed after saving to disk to reduce
   * memory and CPU usage. Recordings loaded in playback mode will have all the markers present.
   *
   * @return the markers in this recording
   */
  public List<Marker> getMarkers() {
    return ThreadUtils.withLock(lock.readLock(), () -> markersReadOnly);
  }

  /**
   * Gets the data in this recording. Note: if you need to get the data objects and markers at the same time,
   * use {@link #copyData copyData()} to avoid locking issues (the individual methods each lock, but the lists may
   * change between method calls).
   *
   * <p>In live recordings, this list will not be exhaustive. Data is removed after saving to disk to reduce
   * memory and CPU usage. Recordings loaded in playback mode will have all the data present.
   *
   * @return the data in this recording
   */
  public List<TimestampedData> getData() {
    return ThreadUtils.withLock(lock.readLock(), () -> dataReadOnly);
  }

  /**
   * Safely copies all data from this recording. The target collections will be cleared and overwritten with the data
   * currently in this recording.
   *
   * @param data    the target collection into which to copy the data objects
   * @param markers the target collection into which to copy the event markers
   */
  public void copyData(Collection<TimestampedData> data, Collection<Marker> markers) {
    ThreadUtils.withLock(lock.readLock(), () -> {
      data.clear();
      data.addAll(this.data);

      markers.clear();
      markers.addAll(this.markers);
    });
  }

  /**
   * Clears the data and markers to free memory. This should <strong>ONLY</strong> be called by the recording mechanism
   * after the current data is saved to disk.
   */
  @SuppressWarnings("PMD.DefaultPackage")
  // This should only be used by the Serialization class in the same package
  void clear() {
    ThreadUtils.withLock(lock.writeLock(), () -> {
      data.clear();
      markers.clear();
    });
  }

  @SuppressWarnings("JavadocMethod")
  public List<String> getSourceIds() {
    return ThreadUtils.withLock(lock.readLock(), () -> sourceIdsReadOnly);
  }

  @SuppressWarnings("JavadocMethod")
  public TimestampedData getFirst() {
    return ThreadUtils.withLock(lock.readLock(), () -> first);
  }

  @SuppressWarnings("JavadocMethod")
  public TimestampedData getLast() {
    return ThreadUtils.withLock(lock.readLock(), () -> last);
  }

  /**
   * Gets the length of this recording in milliseconds. Recordings wth 0 or 1 data points have a length of 0.
   */
  public long getLength() {
    return ThreadUtils.withLock(lock.readLock(), () -> {
      if (first == null || last == null) {
        return 0L;
      } else {
        return last.getTimestamp() - first.getTimestamp();
      }
    });
  }

  @Override
  public boolean equals(Object obj) {
    try {
      lock.readLock().lock();
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      Recording that = (Recording) obj;
      try {
        that.lock.readLock().lock();
        return this.data.equals(that.data)
            && this.markers.equals(that.markers);
      } finally {
        that.lock.readLock().unlock();
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public int hashCode() {
    return ThreadUtils.withLock(lock.readLock(), () -> Objects.hash(data, markers));
  }

  @Override
  public String toString() {
    return ThreadUtils.withLock(lock.readLock(), () -> "Recording(data=" + data + ", markers=" + markers + ")");
  }

}
