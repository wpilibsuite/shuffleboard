package edu.wpi.first.shuffleboard.app.sources.recording;

import edu.wpi.first.shuffleboard.api.data.DataTypes;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

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
  public void testStringToBytes() {
    String empty = "";
    byte[] expected = {0, 0, 0, 0};
    assertArrayEquals(expected, Serialization.toByteArray(empty));

    String foo = "foo";
    expected = new byte[]{0, 0, 0, 3, 'f', 'o', 'o'};
    assertArrayEquals(expected, Serialization.toByteArray(foo));
  }

  @Test
  public void testStringFromBytes() {
    byte[] empty = new byte[]{0, 0, 0, 0};
    assertEquals("", Serialization.readString(empty, 0));

    byte[] foo = new byte[]{0, 0, 0, 3, 'f', 'o', 'o'};
    assertEquals("foo", Serialization.readString(foo, 0));
  }

  @Test
  public void testStringArrayToBytes() {
    String[] empty = {};
    byte[] expected = {0, 0, 0, 0};
    assertArrayEquals(expected, Serialization.toByteArray(empty));

    String[] fooBar = {"foo", "bar"};
    assertArrayEquals(fooBarBytes, Serialization.toByteArray(fooBar));
  }

  @Test
  public void testStringArrayFromBytes() {
    byte[] empty = new byte[4];
    String[] expected = {};
    assertArrayEquals(expected, Serialization.readStringArray(empty, 0));

    expected = new String[]{"foo", "bar"};
    assertArrayEquals(expected, Serialization.readStringArray(fooBarBytes, 0));
  }

  @Test
  public void testEncodeRecode() throws IOException {
    final File file = Files.createTempFile("testEncodeRecode", "frc").toFile();
    final Recording recording = new Recording();
    recording.append(new TimestampedData("foo", DataTypes.String, "bar", 0));
    recording.append(new TimestampedData("foo", DataTypes.String, "baz", 1));
    Serialization.saveRecording(recording, file.getAbsolutePath());
    final Recording loaded = Serialization.loadRecording(file.getAbsolutePath());
    assertEquals("The loaded recording differs from the encoded one", recording, loaded);
  }

}
