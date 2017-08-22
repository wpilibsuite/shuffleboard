package edu.wpi.first.shuffleboard.plugin.base.recording.serialization;

import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringType;

import java.util.Arrays;

public class StringArrayAdapter extends TypeAdapter<String[]> {

  public StringArrayAdapter() {
    super(new StringArrayType());
  }

  @Override
  public byte[] serialize(String[] data) {
    if (data.length == 0) {
      return new byte[Serialization.SIZE_OF_INT];
    }
    byte[] buf = new byte[getSerializedSize(data)];

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
      String string = Serialization.decode(array, cursor, new StringType());
      stringArray[i] = string;
      cursor += Serialization.SIZE_OF_INT + string.length();
    }
    return stringArray;
  }

  @Override
  public int getSerializedSize(String[] value) {
    return Serialization.SIZE_OF_INT
        + Arrays.stream(value)
        .mapToInt(String::length)
        .map(i -> i + Serialization.SIZE_OF_INT)
        .sum();
  }

}
