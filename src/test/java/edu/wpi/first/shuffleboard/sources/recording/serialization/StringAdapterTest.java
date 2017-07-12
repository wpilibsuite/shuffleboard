package edu.wpi.first.shuffleboard.sources.recording.serialization;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringAdapterTest {

  private final StringAdapter adapter = new StringAdapter();

  @Test
  public void testEncodeEmpty() {
    String string = "";
    byte[] expected = {0, 0, 0, 0};
    assertArrayEquals(expected, adapter.serialize(string));
  }

  @Test
  public void testDecodeEmpty() {
    byte[] buffer = {0, 0, 0, 0};
    String expected = "";
    assertEquals(expected, adapter.deserialize(buffer, 0));
  }

  @Test
  public void testEncode() {
    String string = "abcd";
    byte[] expected = {0, 0, 0, 4, 'a', 'b', 'c', 'd'};
    assertArrayEquals(expected, adapter.serialize(string));
  }

  @Test
  public void testDecode() {
    byte[] buffer = {0, 0, 0, 3, 'b', 'a', 'z'};
    String expected = "baz";
    assertEquals(expected, adapter.deserialize(buffer, 0));
  }

}
