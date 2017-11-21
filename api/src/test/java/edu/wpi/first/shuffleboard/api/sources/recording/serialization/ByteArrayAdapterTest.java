package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayAdapterTest {

  private final ByteArrayAdapter adapter = new ByteArrayAdapter();

  @Test
  public void testEncodeEmptyArray() {
    byte[] array = {};
    byte[] expected = {0, 0, 0, 0};
    assertArrayEquals(expected, adapter.serialize(array));
  }

  @Test
  public void testDecodeEmptyArray() {
    byte[] buffer = {0, 0, 0, 0};
    byte[] expected = {};
    assertArrayEquals(expected, adapter.deserialize(buffer, 0));
  }

  @Test
  public void testEncode() {
    byte[] array = {1, 2, 3, 4, 0x7F, (byte) 0x80, (byte) 0xFF};
    byte[] expected = {0, 0, 0, 7, 1, 2, 3, 4, 0x7F, (byte) 0x80, (byte) 0xFF};
    assertArrayEquals(expected, adapter.serialize(array));
  }

  @Test
  public void testDecode() {
    byte[] array = {0, 0, 0, 7, 1, 2, 3, 4, 0x7F, (byte) 0x80, (byte) 0xFF};
    byte[] expected = {1, 2, 3, 4, 0x7F, (byte) 0x80, (byte) 0xFF};
    assertArrayEquals(expected, adapter.deserialize(array, 0));
  }

  @Test
  public void testSerializedSizeEmpty() {
    assertEquals(4, adapter.getSerializedSize(new byte[0]));
  }

  @Test
  public void testSerializedSizeNotEmpty() {
    byte[] array = {1, 2, 3};
    assertEquals(7, adapter.getSerializedSize(array));
  }

}
