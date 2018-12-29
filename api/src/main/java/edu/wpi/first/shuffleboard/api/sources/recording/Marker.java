package edu.wpi.first.shuffleboard.api.sources.recording;

import java.util.Objects;

/**
 * Marks an event in a recording.
 */
public final class Marker implements RecordingEntry {

  private final String name;
  private final String description;
  private final MarkerImportance importance;
  private final long timestamp;

  /**
   * Creates a new event marker.
   *
   * @param name        the name of the marked event
   * @param description a description of the marked event
   * @param importance  the importance of the event
   * @param timestamp   the time the event occurred, measured in milliseconds since the start of the recording
   */
  public Marker(String name, String description, MarkerImportance importance, long timestamp) {
    this.name = Objects.requireNonNull(name, "name");
    this.description = Objects.requireNonNull(description, "description");
    this.importance = Objects.requireNonNull(importance, "importance");
    this.timestamp = timestamp;
  }

  /**
   * Creates a new event marker with no description.
   *
   * @param name       the name of the marked event
   * @param importance the importance of the event
   * @param timestamp  the time the event occurred, measured in milliseconds since the start of the recording
   */
  public Marker(String name, MarkerImportance importance, long timestamp) {
    this(name, "", importance, timestamp);
  }

  /**
   * Gets the name of the marked event.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the description of the marked event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the importance of the marked event.
   */
  public MarkerImportance getImportance() {
    return importance;
  }

  /**
   * Gets the timestamp of the marked event.
   */
  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return String.format("Marker(name=\"%s\", description=\"%s\", importance=%s, timestamp=%d)",
        name, description, importance, timestamp);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Marker that = (Marker) obj;
    return this.name.equals(that.name)
        && this.description.equals(that.description)
        && this.importance.equals(that.importance)
        && this.timestamp == that.timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, importance, timestamp);
  }
}
