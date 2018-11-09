package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.data.DataTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializationTest {

  private static final byte[] fooBarBytes = new byte[]{
      0, 0, 0, 2,                // array length
      0, 0, 0, 3, 'f', 'o', 'o', // "foo", encoded with length
      0, 0, 0, 3, 'b', 'a', 'r'  // "bar", encoded with length
  };

  // Grinning emoji, four bytes
  private static final String grinningEmoji = "😁";

  @Test
  public void testIntToBytes() {
    final int val = 0x007F10FF;
    byte[] bytes = Serialization.toByteArray(val);
    assertArrayEquals(new byte[]{0x00, 0x7F, 0x10, (byte) 0xFF}, bytes);
  }

  @Test
  public void testIntFromBytes() {
    final byte[] bytes = {0x00, 0x10, (byte) 0xFF, (byte) 0xAB};
    int read = Serialization.readInt(bytes);
    assertEquals(0x0010FFAB, read);
  }

  @Test
  public void testLongToBytes() {
    final long val = Long.MAX_VALUE;
    byte[] bytes = Serialization.toByteArray(val);
    assertArrayEquals(
        new byte[]{0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
        bytes);
  }

  @Test
  public void testLongFromBytes() {
    final long val = Long.MAX_VALUE / 7;
    byte[] bytes = Serialization.toByteArray(val);
    long read = Serialization.readLong(bytes);
    assertEquals(val, read);
  }

  @Test
  public void testBooleanToBytes() {
    assertArrayEquals(new byte[]{1}, Serialization.toByteArray(true));
    assertArrayEquals(new byte[]{0}, Serialization.toByteArray(false));
  }

  @Test
  @ExtendWith(TempDirectory.class)
  public void testSimpleEncodeRecode(@TempDir Path dir) throws IOException {
    final Path file = dir.resolve("testSimpleEncodeRecode.sbr");
    final Recording recording = new Recording();
    recording.append(new TimestampedData("foo", DataTypes.Number, 0.0, 0));
    recording.append(new TimestampedData("foo", DataTypes.Number, 100.0, 1));
    ArrayList<TimestampedData> data = new ArrayList<>(recording.getData());
    Serialization.saveRecording(recording, file);
    final Recording loaded = Serialization.loadRecording(file);
    assertEquals(data, loaded.getData(), "The loaded recording differs from the encoded one");
  }

  @Test
  @ExtendWith(TempDirectory.class)
  public void testUpdateFile(@TempDir Path dir) throws IOException {
    final Path file = dir.resolve("testUpdateFile.sbr");
    final Recording recording = new Recording();
    final List<TimestampedData> data = new ArrayList<>();
    data.add(new TimestampedData("foo", DataTypes.Number, 42.0, 0));
    data.add(new TimestampedData("bar", DataTypes.Boolean, true, 1));
    data.forEach(recording::append);
    Serialization.saveRecording(recording, file);
    final Recording loaded = Serialization.loadRecording(file);
    assertEquals(data, loaded.getData(), "The loaded recording differs from the encoded one");

    TimestampedData newData = new TimestampedData("foo", DataTypes.Number, 123.456, 2);
    data.add(newData);
    recording.append(newData);
    Serialization.updateRecordingSave(recording, file);
    final Recording loadedUpdate = Serialization.loadRecording(file);
    assertEquals(data, loadedUpdate.getData());
  }

  @Test
  @ExtendWith(TempDirectory.class)
  public void testUpdateFileWithNewDataTypes(@TempDir Path dir) throws IOException {
    final Path file = dir.resolve("testUpdateFileWithNewDataTypes.sbr");
    final Recording recording = new Recording();
    final List<TimestampedData> data = new ArrayList<>();

    // Initial data: a single String
    TimestampedData initial = new TimestampedData("bar", DataTypes.String, "baz", 0);
    data.add(initial);
    recording.append(initial);
    Serialization.saveRecording(recording, file);
    final Recording loaded = Serialization.loadRecording(file);
    assertEquals(data, loaded.getData(), "Initial data was wrong");

    // First update: add a number and a boolean
    TimestampedData newData = new TimestampedData("foo", DataTypes.Number, 123.0, 1);
    TimestampedData another = new TimestampedData("another", DataTypes.Boolean, false, 2);
    data.add(newData);
    data.add(another);
    recording.append(newData);
    recording.append(another);
    Serialization.updateRecordingSave(recording, file);
    final Recording loadedUpdate = Serialization.loadRecording(file);
    assertEquals(data, loadedUpdate.getData(), "First update was wrong");

    // Second update: add two more booleans. Makes sure there's no constant pool duplication/overwriting
    TimestampedData newBoolean = new TimestampedData("x", DataTypes.Boolean, false, 3);
    TimestampedData anotherBool = new TimestampedData("y", DataTypes.Boolean, true, 4);
    data.add(newBoolean);
    data.add(anotherBool);
    recording.append(newBoolean);
    recording.append(anotherBool);
    Serialization.updateRecordingSave(recording, file);
    final Recording last = Serialization.loadRecording(file);
    assertEquals(data, last.getData(), "Second update was wrong");
  }

  @Test
  @ExtendWith(TempDirectory.class)
  public void testEncodeRecodeWithMarkers(@TempDir Path dir) throws IOException {
    final Path file = dir.resolve("testEncodeRecodeWithMarkers.sbr");
    Recording recording = new Recording();
    recording.addMarker(new Marker("First", "", MarkerImportance.TRIVIAL, 0));
    recording.addMarker(new Marker("Second", "The second marker", MarkerImportance.CRITICAL, 1));
    List<TimestampedData> originalData = new ArrayList<>(recording.getData());
    List<Marker> originalMarkers = new ArrayList<>(recording.getMarkers());
    Serialization.saveRecording(recording, file);
    Recording loaded = Serialization.loadRecording(file);
    assertAll(
        () -> assertEquals(originalData, loaded.getData(), "Data was wrong"),
        () -> assertEquals(originalMarkers, loaded.getMarkers(), "Markers were wrong")
    );
  }

  @Test
  @ExtendWith(TempDirectory.class)
  public void testWithMarkersAndData(@TempDir Path dir) throws IOException {
    final Path file = dir.resolve("testWithMarkersAndData.sbr");
    Recording recording = new Recording();
    recording.append(new TimestampedData("foo", DataTypes.Boolean, false, 0));
    recording.addMarker(new Marker("M1", "Loop iteration 643", MarkerImportance.TRIVIAL, 0));
    recording.addMarker(new Marker("M2", "Loop iteration 644", MarkerImportance.CRITICAL, 5));
    var snapshot = recording.takeSnapshot();
    Serialization.saveRecording(recording, file);
    Recording loaded = Serialization.loadRecording(file);
    assertAll(
        () -> assertEquals(snapshot.getData(), loaded.getData(), "Data was wrong"),
        () -> assertEquals(snapshot.getMarkers(), loaded.getMarkers(), "Markers were wrong")
    );
  }

  @Test
  @ExtendWith(TempDirectory.class)
  public void testUpdateWithMarkersAndData(@TempDir Path dir) throws IOException {
    final Path file = dir.resolve("testUpdateWithMarkersAndData.sbr");
    final TimestampedData data = new TimestampedData("foo", DataTypes.Boolean, false, 0);
    final Marker marker1 = new Marker("M1", "", MarkerImportance.TRIVIAL, 0);
    final Marker marker2 = new Marker("M2", "Loop iteration 644", MarkerImportance.CRITICAL, 5);
    final Marker marker3 = new Marker("M3", "Loop iteration 12312", MarkerImportance.NORMAL, 12);

    Recording recording = new Recording();
    recording.append(data);
    recording.addMarker(marker1);
    recording.addMarker(marker2);

    Serialization.updateRecordingSave(recording, file);

    recording.addMarker(marker3);
    Serialization.updateRecordingSave(recording, file);

    Recording loaded = Serialization.loadRecording(file);
    assertAll(
        () -> assertEquals(List.of(data), loaded.getData(), "Data was wrong"),
        () -> assertEquals(List.of(marker1, marker2, marker3), loaded.getMarkers(), "Markers were wrong")
    );
  }

  @Test
  public void testMultiByteCharsInString() {
    String string = grinningEmoji;
    byte[] bytes = Serialization.toByteArray(string);
    assertEquals(8, bytes.length);
    String read = Serialization.readString(bytes, 0);
    assertEquals(string, read);
  }

  @Test
  public void testMultiByteCharsInStringArray() {
    String[] strings = {
        "®",
        "©",
        grinningEmoji
    };
    byte[] bytes = Serialization.toByteArray(strings);
    assertEquals(24, bytes.length);
    String[] read = Serialization.readStringArray(bytes, 0);
    assertArrayEquals(strings, read);
  }

}
