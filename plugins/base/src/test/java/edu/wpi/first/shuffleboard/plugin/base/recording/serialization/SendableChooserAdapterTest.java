package edu.wpi.first.shuffleboard.plugin.base.recording.serialization;

import edu.wpi.first.shuffleboard.plugin.base.data.SendableChooserData;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class SendableChooserAdapterTest {

  private final SendableChooserAdapter adapter = new SendableChooserAdapter();

  @Test
  public void testEmpty() {
    SendableChooserData data = new SendableChooserData(new String[0], "", "");
    byte[] expected = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    assertArrayEquals(expected, adapter.serialize(data));
  }

  @Test
  public void testEncode() {
    SendableChooserData data = new SendableChooserData(new String[]{"a", "b", "c"}, "a", "c");
    byte[] expected = {0, 0, 0, 3, 0, 0, 0, 1, 'a', 0, 0, 0, 1, 'b', 0, 0, 0, 1, 'c', 0, 0, 0, 1, 'a', 0, 0, 0, 1, 'c'};
    assertArrayEquals(expected, adapter.serialize(data));
  }

  @Test
  public void testEncodeDecode() {
    SendableChooserData data = new SendableChooserData(new String[]{"foo", "bar", "baz"}, "foo", "bar");
    assertEquals(data, adapter.deserialize(adapter.serialize(data), 0));
  }

}
