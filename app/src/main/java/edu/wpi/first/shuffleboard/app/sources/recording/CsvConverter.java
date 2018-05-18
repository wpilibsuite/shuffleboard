package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.sources.recording.ConversionSettings;
import edu.wpi.first.shuffleboard.api.sources.recording.Converter;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvConverter implements Converter {

  public static final CsvConverter Instance = new CsvConverter();

  // How far apart data points can be (in milliseconds) to group them all into a single row
  // We do this because data may be sent at the same time from the source, but high CPU usage
  // or network issues can cause them to arrive over the course of several milliseconds.
  // This value is set to be able to collate data that gets updated 100 times per second without
  // pulling data from two separate update events into a single row
  private static final int TIME_WINDOW = 7; // milliseconds

  private CsvConverter() {
  }

  @Override
  public String formatName() {
    return "CSV";
  }

  @Override
  public String fileExtension() {
    return ".csv";
  }

  @Override
  @SuppressWarnings("LocalVariableName")
  @SuppressFBWarnings(value = "UC_USELESS_OBJECT", justification = "False positive with List.forEach")
  public void export(Recording recording, Path destination, ConversionSettings settings) throws IOException {
    List<String> header = makeHeader(recording, settings);
    int headerSize = header.size();
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(header.toArray(new String[headerSize]));

    try (Writer writer = new OutputStreamWriter(new FileOutputStream(destination.toFile()), "UTF-8");
         CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {

      List<TimestampedData> data = recording.getData();
      boolean skipMetadata = !settings.isConvertMetadata();

      for (int i = 0; i < data.size(); ) {
        TimestampedData point = data.get(i);
        if (skipMetadata && DataSourceUtils.isMetadata(point.getSourceId())) {
          i++;
          continue;
        }
        int j = i;
        List<TimestampedData> row = new ArrayList<>();
        // Collate data within a certain delta time to the same row, since there may be some time jitter
        // for multiple recorded data points that were updated at the same time, but network latencies
        // or CPU usage caused the timestamps to be slightly different
        for (; j < data.size() && data.get(j).getTimestamp() <= point.getTimestamp() + TIME_WINDOW; j++) {
          TimestampedData d = data.get(j);
          if (!(skipMetadata && DataSourceUtils.isMetadata(d.getSourceId()))) {
            row.add(d);
          }
        }
        Object[] rowToSave = new Object[headerSize];
        row.forEach(d -> rowToSave[header.indexOf(d.getSourceId())] = d.getData());
        rowToSave[0] = point.getTimestamp();
        printer.printRecord(rowToSave);
        i = j;
      }
    }
  }

  private List<String> makeHeader(Recording recording, ConversionSettings settings) {
    List<String> header = new ArrayList<>(recording.getSourceIds());
    if (!settings.isConvertMetadata()) {
      header.removeIf(DataSourceUtils::isMetadata);
    }
    header.sort(AlphanumComparator.INSTANCE);
    header.add(0, "Timestamp");
    return header;
  }

}
