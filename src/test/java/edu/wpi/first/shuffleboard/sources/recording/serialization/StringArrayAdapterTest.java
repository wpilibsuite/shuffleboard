package edu.wpi.first.shuffleboard.sources.recording.serialization;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringArrayAdapterTest {

  private final StringArrayAdapter adapter = new StringArrayAdapter();

  @Test
  public void testEncodeEmpty() {
    String[] array = {};
    byte[] expected = {0, 0, 0, 0};
    assertArrayEquals(expected, adapter.serialize(array));
  }

  @Test
  public void testDecodeEmpty() {
    byte[] buffer = {0, 0, 0, 0};
    String[] expected = {};
    assertArrayEquals(expected, adapter.deserialize(buffer, 0));
  }

  @Test
  public void testEncode() {
    String[] array = {"foo", "bar"};
    byte[] expected = {0, 0, 0, 2, 0, 0, 0, 3, 'f', 'o', 'o', 0, 0, 0, 3, 'b', 'a', 'r'};
    assertArrayEquals(expected, adapter.serialize(array));
  }

  @Test
  public void testDecode() {
    byte[] buffer = {0, 0, 0, 2, 0, 0, 0, 3, 'f', 'o', 'o', 0, 0, 0, 3, 'b', 'a', 'r'};
    String[] expected = {"foo", "bar"};
    assertArrayEquals(expected, adapter.deserialize(buffer, 0));
  }

}
