package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.sources.recording.Exporter;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class CsvExporter implements Exporter {

  private final CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("Source", "Value", "Timestamp", "Data type");

  @Override
  public String formatName() {
    return "CSV";
  }

  /**
   * Exports a recording to a file.
   *
   * @param recording   the recording to export
   * @param destination the destination file to export to
   *
   * @throws IOException if the file could not be written
   */
  @Override
  public void export(Recording recording, Path destination) throws IOException {
    try (FileWriter writer = new FileWriter(destination.toFile());
         CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
      for (TimestampedData data : recording.getData()) {
        printer.printRecord(
            data.getSourceId(),
            data.getData().toString(),
            data.getTimestamp(),
            data.getDataType().getName()
        );
      }
    }
  }

}
