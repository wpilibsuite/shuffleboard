package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.util.concurrent.FunctionalReadWriteLock;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Recording {

  private TimestampedData first;
  private TimestampedData last;
  private final List<TimestampedData> data = Collections.synchronizedList(new ArrayList<>());
  private final List<Marker> markers = Collections.synchronizedList(new ArrayList<>());
  private final List<String> sourceIds = new ArrayList<>();

  private final FunctionalReadWriteLock lock = FunctionalReadWriteLock.createReentrant();

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
    lock.writing(() -> markers.add(marker));
  }

  /**
   * Gets the markers in this recording. Note: if you need to get the data objects and markers at the same time,
   * use {@link #takeSnapshot takeSnapshot()} to avoid locking issues (the individual methods each lock, but the lists
   * may change between method calls).
   *
   * <p>In live recordings, this list will not be exhaustive. Markers are removed after saving to disk to reduce
   * memory and CPU usage. Recordings loaded in playback mode will have all the markers present.
   *
   * @return an immutable list of the markers in this recording
   */
  public List<Marker> getMarkers() {
    return lock.reading(() -> ImmutableList.copyOf(markers));
  }

  /**
   * Gets the data in this recording. Note: if you need to get the data objects and markers at the same time,
   * use {@link #takeSnapshot takeSnapshot()} to avoid locking issues (the individual methods each lock, but the lists
   * may change between method calls).
   *
   * <p>In live recordings, this list will not be exhaustive. Data is removed after saving to disk to reduce
   * memory and CPU usage. Recordings loaded in playback mode will have all the data present.
   *
   * @return an immutable list of the data in this recording
   */
  public List<TimestampedData> getData() {
    return lock.reading(() -> ImmutableList.copyOf(data));
  }

  /**
   * Takes a snapshot of this recording. The snapshot is immutable and thread-safe.
   *
   * @return a snapshot of this recording
   */
  public Snapshot takeSnapshot() {
    return lock.reading(() -> Snapshot.of(this));
  }

  /**
   * Takes a snapshot of this recording, then clears the data in this recording. This should <strong>ONLY</strong>
   * be called by the recording mechanism.
   *
   * @return a snapshot of this recording
   */
  @SuppressWarnings("PMD.DefaultPackage")
  // This should only be used by the Serialization class in the same package
  Snapshot takeSnapshotAndClear() {
    return lock.writing(() -> {
      var snapshot = Snapshot.of(this);
      data.clear();
      markers.clear();
      return snapshot;
    });
  }

  @SuppressWarnings("JavadocMethod")
  public List<String> getSourceIds() {
    return lock.reading(() -> ImmutableList.copyOf(sourceIds));
  }

  @SuppressWarnings("JavadocMethod")
  public TimestampedData getFirst() {
    return lock.reading(() -> first);
  }

  @SuppressWarnings("JavadocMethod")
  public TimestampedData getLast() {
    return lock.reading(() -> last);
  }

  /**
   * Gets the length of this recording in milliseconds. Recordings wth 0 or 1 data points have a length of 0.
   */
  public long getLength() {
    return lock.reading(() -> {
      if (first == null || last == null) {
        return 0L;
      } else {
        return last.getTimestamp() - first.getTimestamp();
      }
    });
  }

  @Override
  public String toString() {
    return lock.reading(() -> "Recording(data=" + data + ", markers=" + markers + ")");
  }

  /**
   * A snapshot of the state of a recording. Snapshots are immutable and thread-safe.
   */
  public static final class Snapshot {
    private final ImmutableList<TimestampedData> data;
    private final ImmutableList<Marker> markers;

    /**
     * Snapshots a recording. Note: this should only be used by {@link Recording#takeSnapshot()} or
     * {@link Recording#takeSnapshotAndClear()}. This method is not thread-safe.
     *
     * @param recording the recording to create a snapshot of
     *
     * @return a new snapshot
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static Snapshot of(Recording recording) {
      return new Snapshot(
          ImmutableList.copyOf(recording.data),
          ImmutableList.copyOf(recording.markers)
      );
    }

    private Snapshot(ImmutableList<TimestampedData> data, ImmutableList<Marker> markers) {
      this.data = data;
      this.markers = markers;
    }

    public ImmutableList<TimestampedData> getData() {
      return data;
    }

    public ImmutableList<Marker> getMarkers() {
      return markers;
    }
  }

}
