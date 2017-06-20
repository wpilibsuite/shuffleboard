package edu.wpi.first.shuffleboard.sources.recording;


import java.util.Objects;

/**
 * Represents an immutable view of the value of a data source at a specific instant.
 */
public final class TimestampedData implements Comparable<TimestampedData> {

  private final String sourceId;
  private final Object data;
  private final long timestamp;

  /**
   * Creates a new time stamped data object.
   *
   * @param sourceId  the ID of the source corresponding to this data
   * @param data      the actual data
   * @param timestamp the timestamp for when the data was recorded
   */
  public TimestampedData(String sourceId, Object data, long timestamp) {
    this.sourceId = sourceId;
    this.data = data;
    this.timestamp = timestamp;
  }

  public String getSourceId() {
    return sourceId;
  }

  public Object getData() {
    return data;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    TimestampedData that = (TimestampedData) other;
    return this.sourceId.equals(that.sourceId)
        && this.data.equals(that.data)
        && this.timestamp == that.timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceId, data, timestamp);
  }


  @Override
  public String toString() {
    return String.format("TimeStampedData(sourceId=%s, data=%s, timestamp=%s)",
        sourceId, data, timestamp);
  }

  @Override
  public int compareTo(TimestampedData other) {
    if (this.timestamp == other.timestamp) {
      return this.sourceId.compareTo(other.sourceId);
    } else {
      return this.timestamp > other.timestamp ? 1 : -1;
    }
  }

}
