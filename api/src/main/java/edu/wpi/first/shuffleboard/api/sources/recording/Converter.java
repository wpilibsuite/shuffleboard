package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles converting of recording files to a file. These are useful for making it easier to analyze recorded data with
 * third-party tools like Excel or other spreadsheet applications.
 *
 * <p>Converters can use any format, human-readable or not.
 */
public interface Converter {

  /**
   * Gets the name of the format that recordings are exported to.
   */
  String formatName();

  /**
   * The file extension for the output files, including the dot. For example, ".csv" or ".txt" for textual output files.
   */
  String fileExtension();

  /**
   * Converts a recording, then exports the result to a file.
   *
   * @param recording   the recording to export
   * @param destination the destination file to export to
   * @param settings    a container object for additional settings to use in the conversion
   *
   * @throws IOException if the file could not be written
   */
  void export(Recording recording, Path destination, ConversionSettings settings) throws IOException;

  /**
   * Flattens recording entries into a single map of timestamp-to-data. Note that each timestamp is expected to have
   * no more than ONE event marker mapped to it.
   *
   * @param recording the recording to flatten
   * @param settings  the conversion settings to use
   * @param window    the time window within which temporally-close data should be considered to have the same
   *                  timestamp. A value of zero will result in only entries for that timestamp being mapped to it;
   *                  higher values loosens this restriction to accommodate potential variances in timestamps.
   *
   * @return a flattened view the timestamped entries in the recording
   *
   * @throws IllegalArgumentException if {@code window} is negative
   */
  @SuppressWarnings("LocalVariableName")
  static Map<Long, List<RecordingEntry>> flatten(Recording recording, ConversionSettings settings, long window) {
    if (window < 0) {
      throw new IllegalArgumentException("Time window must be non-negative, given " + window);
    }

    if (recording.getData().isEmpty() && recording.getMarkers().isEmpty()) {
      // No data or events
      return Map.of();
    }

    // All the timestamped data and event markers, sorted by timestamp in ascending order (oldest data first)
    final List<RecordingEntry> data = Stream.concat(
        recording.getData().stream(),
        recording.getMarkers().stream())
        .sorted(Comparator.comparingLong(RecordingEntry::getTimestamp))
        .collect(Collectors.toList());

    final Map<Long, List<RecordingEntry>> map = new HashMap<>();
    final boolean skipMetadata = !settings.isConvertMetadata();

    for (int i = 0; i < data.size(); ) {
      RecordingEntry point = data.get(i);
      if (skipMetadata && isMetadata(point)) {
        // Skip metadata
        i++;
        continue;
      }

      int j = i;
      List<RecordingEntry> elements = new ArrayList<>();
      // Collate data within a certain delta time to the same collection, since there may be some time jitter
      // for multiple recorded data points that were updated at the same time, but network latencies
      // or CPU usage caused the timestamps to be slightly different
      for (; j < data.size() && data.get(j).getTimestamp() <= point.getTimestamp() + window; j++) {
        var e = data.get(j);
        if (skipMetadata && isMetadata(e)) {
          continue;
        }
        elements.add(e);
      }

      // Place the marker (if present) at the beginning
      // Note: assumes only one marker is present per slice, so the time window must
      // be narrow enough to preserve this behavior
      for (int k = elements.size() - 1; k >= 0; k--) {
        if (elements.get(k) instanceof Marker) {
          var element = elements.remove(k);
          elements.add(0, element);
          break;
        }
      }
      map.put(point.getTimestamp(), elements);
      i = j;
    }
    return map;
  }

  /**
   * Checks if a recording entry is metadata.
   *
   * @param entry the entry to check
   *
   * @return true if the entry is metadata, false if not
   */
  private static boolean isMetadata(RecordingEntry entry) {
    if (entry instanceof TimestampedData) {
      var data = (TimestampedData) entry;
      return DataSourceUtils.isMetadata(data.getSourceId());
    }
    return false;
  }

}
