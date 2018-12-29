package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.ConversionSettings;
import edu.wpi.first.shuffleboard.api.sources.recording.Marker;
import edu.wpi.first.shuffleboard.api.sources.recording.MarkerImportance;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvConverterTest {

  private static final ConversionSettings WITHOUT_METADATA = new ConversionSettings(false);

  private static final String EMPTY_HEADER = "Timestamp,Event,Event Description,Event Severity";

  private CsvConverter converter;
  private Recording recording;

  @BeforeEach
  public void setup() {
    converter = CsvConverter.Instance;
    recording = new Recording();
  }

  @Test
  public void testDataAndMarkerInSameWindow() {
    recording.append(new TimestampedData("foo", DataTypes.String, "bar", 0));
    recording.addMarker(new Marker("Name", "Description", MarkerImportance.CRITICAL, 4L));

    String csv = converter.convertToCsv(recording, WITHOUT_METADATA);
    var lines = csv.lines().collect(Collectors.toList());

    assertAll(
        () -> assertEquals(EMPTY_HEADER + ",foo", lines.get(0)),
        () -> assertEquals("0,Name,Description,CRITICAL,bar", lines.get(1))
    );
  }

  @Test
  public void testMultipleMarkersInSameWindow() {
    // First two markers are in the same time window
    // The second marker should be skipped, but the first and third should still be present
    recording.addMarker(new Marker("First", "", MarkerImportance.LOW, 0L));
    recording.addMarker(new Marker("Second", "", MarkerImportance.CRITICAL, 0L));
    recording.addMarker(new Marker("Third", "", MarkerImportance.NORMAL, 255));

    String csv = converter.convertToCsv(recording, WITHOUT_METADATA);
    var lines = csv.lines().collect(Collectors.toList());

    assertAll(
        () -> assertEquals(EMPTY_HEADER, lines.get(0), "First line should be the header"),
        () -> assertEquals("0,First,,LOW", lines.get(1), "Second line should be the first marker"),
        () -> assertEquals("255,Third,,NORMAL", lines.get(2), "Third line should be the third marker")
    );
  }

}