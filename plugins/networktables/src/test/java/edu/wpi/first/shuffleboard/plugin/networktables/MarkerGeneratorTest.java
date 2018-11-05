package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.sources.recording.Marker;
import edu.wpi.first.shuffleboard.api.sources.recording.MarkerImportance;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.sleep;

public class MarkerGeneratorTest {

  private NetworkTableInstance ntInstance;
  private Recorder recorder;
  private MarkerGenerator generator;
  private NetworkTableEntry entry;

  @BeforeEach
  public void setup() {
    AsyncUtils.setAsyncRunner(Runnable::run);
    ntInstance = NetworkTableInstance.create();
    ntInstance.setUpdateRate(0.01);
    entry = ntInstance.getEntry(MarkerGenerator.MARKER_ENTRY_KEY);
    recorder = Recorder.createDummyInstance();
    generator = new MarkerGenerator(ntInstance, recorder);
  }

  @AfterEach
  public void tearDown() {
    NetworkTableUtils.shutdown(ntInstance);
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
  }

  @Test
  public void testInvalidEntryType() {
    entry.setBoolean(false);
    assertNoMarkersAdded();
  }

  @Test
  public void testEmptyStringArray() {
    entry.setStringArray(new String[0]);
    assertNoMarkersAdded();
  }

  @Test
  public void testStringArrayTooLarge() {
    entry.setStringArray(new String[4]);
    assertNoMarkersAdded();
  }

  @Test
  public void testEmptyMarkerName() {
    entry.setStringArray(new String[]{"", "description", "severity"});
    assertNoMarkersAdded();
  }

  @Test
  public void testWhitespaceMarkerName() {
    entry.setStringArray(new String[]{"\t\r\n\n ", "description", "severity"});
    assertNoMarkersAdded();
  }

  private void assertNoMarkersAdded() {
    waitForEntry();
    assertEquals(0, recorder.getRecording().getMarkers().size(), "No markers should have been added");
  }

  private void waitForEntry() {
    recorder.start();
    generator.start();
    sleep(50, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testMarkerAdded() {
    String name = "name";
    String description = "description";
    String importance = "trivial";
    entry.setStringArray(new String[]{name, description, importance});
    Marker expected = new Marker(name, description, MarkerImportance.TRIVIAL, 0);
    waitForEntry();
    assertAll(
        () -> assertEquals(1, recorder.getRecording().getMarkers().size(), "One marker should have been added"),
        () -> assertEqualsIgnoreTimestamp(expected, recorder.getRecording().getMarkers().get(0))
    );
  }

  @Test
  public void testNoDescription() {
    String name = "name";
    String importance = "critical";
    entry.setStringArray(new String[]{name, "", importance});
    Marker expected = new Marker(name, "", MarkerImportance.CRITICAL, 0);
    waitForEntry();
    assertAll(
        () -> assertEquals(1, recorder.getRecording().getMarkers().size(), "One marker should have been added"),
        () -> assertEqualsIgnoreTimestamp(expected, recorder.getRecording().getMarkers().get(0))
    );
  }

  private void assertEqualsIgnoreTimestamp(Marker expected, Marker actual) {
    assertAll("Marker did not match",
        () -> assertEquals(expected.getName(), actual.getName(), "Names did not match"),
        () -> assertEquals(expected.getDescription(), actual.getDescription(), "Descriptions did not match"),
        () -> assertEquals(expected.getImportance(), actual.getImportance(), "Importance did not match")
    );
  }
}
