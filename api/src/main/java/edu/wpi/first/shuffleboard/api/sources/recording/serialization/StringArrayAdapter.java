package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;

public class StringArrayAdapter extends TypeAdapter<String[]> {

  public StringArrayAdapter() {
    super(DataTypes.StringArray);
  }

  @Override
  public byte[] serialize(String[] data) {
    if (data.length == 0) {
      return new byte[Serialization.SIZE_OF_INT];
    }
    byte[] buf = new byte[Serialization.sizeOfStringArray(data)];

    int pos = 0;

    Serialization.put(buf, Serialization.toByteArray(data.length), pos);
    pos += Serialization.SIZE_OF_INT;

    for (String string : data) {
      byte[] arr = Serialization.encode(string);
      Serialization.put(buf, arr, pos);
      pos += arr.length;
    }

    return buf;
  }

  @Override
  public String[] deserialize(byte[] array, int pos) {
    int cursor = pos;
    int length = Serialization.readInt(array, cursor);
    cursor += Serialization.SIZE_OF_INT;
    String[] stringArray = new String[length];
    for (int i = 0; i < length; i++) {
      String string = Serialization.readString(array, cursor);
      stringArray[i] = string;
      cursor += Serialization.SIZE_OF_INT + Serialization.readInt(array, cursor);
    }
    return stringArray;
  }

  @Override
  public int getSerializedSize(String[] value) {
    return serialize(value).length;
  }

}
