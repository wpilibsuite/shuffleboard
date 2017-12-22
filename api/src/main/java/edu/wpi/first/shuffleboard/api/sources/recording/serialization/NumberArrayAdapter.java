package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;

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
    Serialization.put(buf, Serialization.toByteArray(array.length), pos);
    pos += Serialization.SIZE_OF_INT;

    for (double val : array) {
      byte[] arr = Serialization.toByteArray(val);
      Serialization.put(buf, arr, pos);
      pos += Serialization.SIZE_OF_DOUBLE;
    }
    return buf;
  }

  @Override
  public double[] deserialize(byte[] buffer, int bufferPosition) {
    int cursor = bufferPosition;
    int length = Serialization.readInt(buffer, cursor);
    cursor += Serialization.SIZE_OF_INT;
    double[] doubleArray = new double[length];
    for (int i = 0; i < length; i++) {
      double val = Serialization.readDouble(buffer, cursor);
      doubleArray[i] = val;
      cursor += Serialization.SIZE_OF_DOUBLE;
    }
    return doubleArray;

  }

  @Override
  public int getSerializedSize(double[] value) {
    return Serialization.SIZE_OF_INT + (Serialization.SIZE_OF_DOUBLE * value.length);
  }
}
