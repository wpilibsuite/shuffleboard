package edu.wpi.first.shuffleboard.sources.recording.serialization;

import edu.wpi.first.shuffleboard.data.DataTypes;
import edu.wpi.first.shuffleboard.sources.recording.Serialization;

import static edu.wpi.first.shuffleboard.sources.recording.Serialization.SIZE_OF_DOUBLE;
import static edu.wpi.first.shuffleboard.sources.recording.Serialization.SIZE_OF_INT;
import static edu.wpi.first.shuffleboard.sources.recording.Serialization.encode;
import static edu.wpi.first.shuffleboard.sources.recording.Serialization.put;
import static edu.wpi.first.shuffleboard.sources.recording.Serialization.toByteArray;

public class NumberArrayAdapter extends TypeAdapter<double[]> {

  public NumberArrayAdapter() {
    super(DataTypes.NumberArray);
  }

  @Override
  public byte[] serialize(double[] array) {
    final byte[] buf = new byte[getSerializedSize(array)];
    if (array.length == 0) {
      return buf;
    }
    int pos = 0;
    put(buf, toByteArray(array.length), pos);
    pos += SIZE_OF_INT;

    for (double val : array) {
      byte[] arr = encode(val);
      put(buf, arr, pos);
      pos += SIZE_OF_DOUBLE;
    }
    return buf;
  }

  @Override
  public double[] deserialize(byte[] buffer, int bufferPosition) {
    int cursor = bufferPosition;
    int length = Serialization.readInt(buffer, cursor);
    cursor += SIZE_OF_INT;
    double[] doubleArray = new double[length];
    for (int i = 0; i < length; i++) {
      double val = Serialization.readDouble(buffer, cursor);
      doubleArray[i] = val;
      cursor += SIZE_OF_DOUBLE;
    }
    return doubleArray;

  }

  @Override
  public int getSerializedSize(double[] value) {
    return SIZE_OF_INT + (SIZE_OF_DOUBLE * value.length);
  }
}
