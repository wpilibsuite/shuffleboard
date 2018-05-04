package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.sources.recording.Converter;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvConverter implements Converter {

  public static final CsvConverter Instance = new CsvConverter();

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
  public void export(Recording recording, Path destination) throws IOException {
    ArrayList<String> header = new ArrayList<>(recording.getSourceIds());
    header.sort(AlphanumComparator.INSTANCE);
    header.add(0, "Timestamp");
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(header.toArray(new String[header.size()]));
    try (FileWriter writer = new FileWriter(destination.toFile());
         CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
      List<TimestampedData> data = recording.getData();
      for (int i = 0; i < data.size(); ) {
        TimestampedData point = data.get(i);
        int j = i;
        List<TimestampedData> row = new ArrayList<>();
        // Collate data within a certain delta time to the same row, since there may be some time jitter
        // for multiple recorded data points that were updated at the same time, but network latencies
        // or CPU usage caused the timestamps to be slightly different
        for (; j < data.size() && data.get(j).getTimestamp() <= point.getTimestamp() + 7; j++) {
          row.add(data.get(j));
        }
        Object[] rowToSave = new Object[header.size()];
        row.forEach(d -> rowToSave[header.indexOf(d.getSourceId())] = d.getData());
        rowToSave[0] = point.getTimestamp();
        printer.printRecord(rowToSave);
        i = j;
      }
    }
  }

}
