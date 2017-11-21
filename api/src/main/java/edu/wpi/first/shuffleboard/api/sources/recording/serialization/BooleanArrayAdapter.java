package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;

public class BooleanArrayAdapter extends TypeAdapter<boolean[]> {

  public BooleanArrayAdapter() {
    super(DataTypes.BooleanArray);
  }

  @Override
  public byte[] serialize(boolean[] array) {
    final byte[] buf = new byte[getSerializedSize(array)];
    if (array.length == 0) {
      return buf;
    }
    int pos = 0;
    Serialization.put(buf, Serialization.toByteArray(array.length), pos);
    pos += Serialization.SIZE_OF_INT;

    for (boolean val : array) {
      byte[] arr = Serialization.toByteArray(val);
      Serialization.put(buf, arr, pos);
      pos += Serialization.SIZE_OF_BOOL;
    }
    return buf;
  }

  @Override
  public boolean[] deserialize(byte[] array, int pos) {
    int cursor = pos;
    int length = Serialization.readInt(array, cursor);
    cursor += Serialization.SIZE_OF_INT;
    boolean[] booleanArray = new boolean[length];
    for (int i = 0; i < length; i++) {
      boolean bool = Serialization.readBoolean(array, cursor);
      booleanArray[i] = bool;
      cursor += Serialization.SIZE_OF_BOOL;
    }
    return booleanArray;
  }

  @Override
  public int getSerializedSize(boolean[] value) {
    return Serialization.SIZE_OF_INT + (value.length * Serialization.SIZE_OF_BOOL);
  }

}
