package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.sources.recording.ConversionSettings;
import edu.wpi.first.shuffleboard.api.sources.recording.Converter;
import edu.wpi.first.shuffleboard.api.sources.recording.Marker;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.sources.recording.RecordingEntry;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.util.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class CsvConverter implements Converter {

  public static final CsvConverter Instance = new CsvConverter();

  // How far apart data points can be (in milliseconds) to group them all into a single row
  // We do this because data may be sent at the same time from the source, but high CPU usage
  // or network issues can cause them to arrive over the course of several milliseconds.
  // This value is set to be able to collate data that gets updated 100 times per second without
  // pulling data from two separate update events into a single row
  private static final int TIME_WINDOW = 7; // milliseconds

  private static final Logger log = Logger.getLogger(CsvConverter.class.getName());
  private static final String invariantViolatedMessageFormat =
      "Invariant violated: multiple event markers for same timestamp (found: %s at entry %d of %d), for timestamp %d";

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
  public void export(Recording recording, Path destination, ConversionSettings settings) throws IOException {
    try (var outputStream = new FileOutputStream(destination.toFile());
         var writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
      writer.append(convertToCsv(recording, settings));
    }
  }

  /**
   * Converts recorded data to CSV text.
   *
   * @param recording the recording to convert
   * @param settings  the conversion settings to use
   *
   * @return a CSV-formatted text string of the data in the recording
   */
  @SuppressFBWarnings(value = "UC_USELESS_OBJECT", justification = "False positive with List.forEach")
  public String convertToCsv(Recording recording, ConversionSettings settings) {
    List<String> header = makeHeader(recording, settings);
    int headerSize = header.size();
    CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(header.toArray(new String[headerSize]));

    try (var writer = new StringWriter();
         var csvPrinter = new CSVPrinter(writer, csvFormat)) {
      var flattenedData = Converter.flatten(recording, settings, TIME_WINDOW);

      var rows = flattenedData.entrySet()
          .stream()
          .sorted(Comparator.comparingLong(Map.Entry::getKey))
          .map(e -> toRow(header, headerSize, e))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      for (Object[] row : rows) {
        csvPrinter.printRecord(row);
      }

      return writer.toString();
    } catch (IOException e) {
      throw new IllegalStateException("Could not convert recording to CSV", e);
    }
  }

  private Object[] toRow(List<String> header, int headerSize, Map.Entry<Long, List<RecordingEntry>> entry) {
    final var entries = entry.getValue();
    if (entries.isEmpty()) {
      return null;
    }
    Object[] row = new Object[headerSize];
    row[0] = entry.getKey();

    int dataStart = 0;

    if (entries.get(0) instanceof Marker) {
      dataStart = 1;

      var marker = (Marker) entries.get(0);
      row[1] = marker.getName();
      row[2] = marker.getDescription();
      row[3] = marker.getImportance();
    }

    for (int i = dataStart; i < entries.size(); i++) {
      if (entries.get(i) instanceof Marker) {
        // Invariant was violated, log it and ignore the marker
        log.warning(
            String.format(
                invariantViolatedMessageFormat,
                entries.get(i),
                i + 1,
                entries.size(),
                entry.getKey()
            )
        );
        continue;
      }
      var point = (TimestampedData) entries.get(i);
      var data = point.getData();
      int index = header.indexOf(point.getSourceId());
      if (data instanceof ComplexData /*c*/) { // TODO pattern matching from Project Amber
        row[index] = ((ComplexData) data).toHumanReadableString();
      } else {
        row[index] = StringUtils.deepToString(data);
      }
    }

    return row;
  }

  private List<String> makeHeader(Recording recording, ConversionSettings settings) {
    List<String> header = new ArrayList<>(recording.getSourceIds());
    if (!settings.isConvertMetadata()) {
      header.removeIf(DataSourceUtils::isMetadata);
    }
    header.sort(AlphanumComparator.INSTANCE);
    header.addAll(0, List.of("Timestamp", "Event", "Event Description", "Event Severity"));
    return header;
  }

}
