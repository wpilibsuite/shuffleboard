package edu.wpi.first.shuffleboard.api.sources.recording;


import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.Objects;

/**
 * Represents an immutable view of the value of a data source at a specific instant.
 */
public final class TimestampedData implements RecordingEntry, Comparable<TimestampedData> {

  private final String sourceId;
  private final DataType dataType;
  private final Object data;
  private final long timestamp;

  /**
   * Creates a new time stamped data object.
   *
   * @param sourceId  the ID of the source corresponding to this data
   * @param dataType  the type of this data
   * @param data      the actual data
   * @param timestamp the timestamp for when the data was recorded
   */
  public TimestampedData(String sourceId, DataType dataType, Object data, long timestamp) {
    this.sourceId = sourceId;
    this.dataType = dataType;
    this.data = data;
    this.timestamp = timestamp;
  }

  public String getSourceId() {
    return sourceId;
  }

  public DataType getDataType() {
    return dataType;
  }

  public Object getData() {
    return data;
  }

  @Override
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
        && this.dataType.equals(that.dataType)
        && this.data.equals(that.data)
        && this.timestamp == that.timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceId, dataType, data, timestamp);
  }


  @Override
  public String toString() {
    return String.format("TimeStampedData(sourceId=%s, dataType=%s, data=%s, timestamp=%s)",
        sourceId, dataType, data, timestamp);
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
