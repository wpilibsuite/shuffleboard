package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class NumberArrayAdapterTest extends AbstractAdapterTest<double[]> {

  public NumberArrayAdapterTest() {
    super(new NumberArrayAdapter(), new ByteArrayAdapter());
  }

  @Test
  public void testEncodeEmpty() {
    double[] array = {};
    byte[] expected = {0, 0, 0, 0};
    assertArrayEquals(expected, adapter.serialize(array));
  }

  @Test
  public void testDecodeEmpty() {
    byte[] buffer = {0, 0, 0, 0};
    double[] expected = {};
    assertArrayEquals(expected, adapter.deserialize(buffer, 0));
  }

  @Test
  public void testEncode() {
    double[] array = {
        Double.MAX_VALUE,
        Double.MIN_VALUE,
        0,
        -Double.MAX_VALUE,
        Double.NaN,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY
    };
    assertArrayEquals(array, adapter.deserialize(adapter.serialize(array), 0));
  }

}
