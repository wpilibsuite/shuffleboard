package edu.wpi.first.shuffleboard.api.sources.recording;

/**
 * Common interface for generic recording entries.
 */
public interface RecordingEntry {
  /**
   * The timestamp of this entry.
   */
  long getTimestamp();
}
