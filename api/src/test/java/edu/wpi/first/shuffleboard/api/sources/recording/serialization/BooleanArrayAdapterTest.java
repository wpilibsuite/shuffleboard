package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class BooleanArrayAdapterTest extends AbstractAdapterTest<boolean[]> {

  public BooleanArrayAdapterTest() {
    super(new BooleanArrayAdapter(), new ByteArrayAdapter());
  }

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
