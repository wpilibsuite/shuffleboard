package edu.wpi.first.shuffleboard.api.sources.recording;

import java.io.IOException;
import java.nio.file.Path;

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

}
