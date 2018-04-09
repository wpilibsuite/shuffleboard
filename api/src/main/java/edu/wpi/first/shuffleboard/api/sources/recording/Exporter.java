package edu.wpi.first.shuffleboard.api.sources.recording;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Handles exporting of recording files to a file. These are useful for making it easier to analyze recorded data with
 * third-party tools like Excel or other spreadsheet applications.
 *
 * <p>Exporters can use any format, human-readable or not.
 */
public interface Exporter {

  /**
   * Gets the name of the format that recordings are exported to.
   */
  String formatName();

  /**
   * Exports a recording to a file.
   *
   * @param recording   the recording to export
   * @param destination the destination file to export to
   *
   * @throws IOException if the file could not be written
   */
  void export(Recording recording, Path destination) throws IOException;

}
