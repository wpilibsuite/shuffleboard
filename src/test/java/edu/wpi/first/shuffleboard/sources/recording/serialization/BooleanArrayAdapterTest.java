package edu.wpi.first.shuffleboard.sources.recording.serialization;

import org.junit.Test;

import static org.junit.Assert.*;

public class BooleanArrayAdapterTest {

  private final BooleanArrayAdapter adapter = new BooleanArrayAdapter();

  @Test
  public void testEncodeEmptyArray() {
    final boolean[] array = {};
    final byte[] serialized = adapter.serialize(array);
    assertArrayEquals(new byte[4], serialized);
  }

  @Test
  public void testDecodeEmptyArray() {
    final byte[] buffer = {0, 0, 0, 0};
    final boolean[] deserialized = adapter.deserialize(buffer, 0);
    assertEquals(0, deserialized.length);
  }

  @Test
  public void testEncode() {
    final boolean[] array = {true, false, true, false};
    final byte[] expected = {
        0, 0, 0, 4,
        1, 0, 1, 0
    };
    final byte[] serialized = adapter.serialize(array);
    assertArrayEquals(expected, serialized);
  }

  @Test
  public void testDecode() {
    final byte[] buffer = {0, 0, 0, 4, 1, 0, 1, 0};
    final boolean[] expected = {true, false, true, false};
    assertArrayEquals(expected, adapter.deserialize(buffer, 0));
  }

}
