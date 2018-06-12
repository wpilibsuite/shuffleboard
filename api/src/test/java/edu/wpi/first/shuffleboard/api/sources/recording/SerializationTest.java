package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.data.DataTypes;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("PMD")
public class SerializationTest {

  private static final byte[] fooBarBytes = new byte[]{
      0, 0, 0, 2,                // array length
      0, 0, 0, 3, 'f', 'o', 'o', // "foo", encoded with length
      0, 0, 0, 3, 'b', 'a', 'r'  // "bar", encoded with length
  };

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
  public void testSimpleEncodeRecode() throws IOException {
    final Path file = Files.createTempFile("testEncodeRecode", "sbr");
    final Recording recording = new Recording();
    recording.append(new TimestampedData("foo", DataTypes.Number, 0.0, 0));
    recording.append(new TimestampedData("foo", DataTypes.Number, 100.0, 1));
    ArrayList<TimestampedData> data = new ArrayList<>(recording.getData());
    Serialization.saveRecording(recording, file);
    final Recording loaded = Serialization.loadRecording(file);
    assertEquals(data, loaded.getData(), "The loaded recording differs from the encoded one");
  }

  @Test
  public void testUpdateFile() throws IOException {
    final Path file = Files.createTempFile("testEncodeRecode", "sbr");
    final Recording recording = new Recording();
    final List<TimestampedData> data = new ArrayList<>();
    data.add(new TimestampedData("foo", DataTypes.Number, 42.0, 0));
    data.add(new TimestampedData("bar", DataTypes.Boolean, true, 1));
    recording.getData().addAll(data);
    Serialization.saveRecording(recording, file);
    final Recording loaded = Serialization.loadRecording(file);
    assertEquals(data, loaded.getData(), "The loaded recording differs from the encoded one");

    TimestampedData newData = new TimestampedData("foo", DataTypes.Number, 123.456, 2);
    data.add(newData);
    recording.getData().add(newData);
    Serialization.updateRecordingSave(recording, file);
    final Recording loadedUpdate = Serialization.loadRecording(file);
    assertEquals(data, loadedUpdate.getData());
  }

}
