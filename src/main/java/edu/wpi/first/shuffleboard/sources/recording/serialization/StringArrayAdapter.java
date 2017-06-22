package edu.wpi.first.shuffleboard.sources.recording.serialization;

import edu.wpi.first.shuffleboard.data.DataTypes;
import edu.wpi.first.shuffleboard.sources.recording.Serialization;

import java.util.Arrays;

import static edu.wpi.first.shuffleboard.sources.recording.Serialization.SIZE_OF_INT;
import static edu.wpi.first.shuffleboard.sources.recording.Serialization.put;
import static edu.wpi.first.shuffleboard.sources.recording.Serialization.readInt;
import static edu.wpi.first.shuffleboard.sources.recording.Serialization.toByteArray;

public class StringArrayAdapter extends TypeAdapter<String[]> {

  public StringArrayAdapter() {
    super(DataTypes.StringArray);
  }

  @Override
  public byte[] serialize(String[] data) {
    if (data.length == 0) {
      return new byte[SIZE_OF_INT];
    }
    byte[] buf = new byte[getSerializedSize(data)];

    int pos = 0;

    put(buf, toByteArray(data.length), pos);
    pos += SIZE_OF_INT;

    for (String string : data) {
      byte[] arr = Serialization.encode(string);
      put(buf, arr, pos);
      pos += arr.length;
    }

    return buf;
  }

  @Override
  public String[] deserialize(byte[] array, int pos) {
    int cursor = pos;
    int length = readInt(array, cursor);
    cursor += SIZE_OF_INT;
    String[] stringArray = new String[length];
    for (int i = 0; i < length; i++) {
      String string = Serialization.decode(array, cursor, DataTypes.String);
      stringArray[i] = string;
      cursor += SIZE_OF_INT + string.length();
    }
    return stringArray;
  }

  @Override
  public int getSerializedSize(String[] value) {
    return SIZE_OF_INT
        + Arrays.stream(value)
        .mapToInt(String::length)
        .map(i -> i + SIZE_OF_INT)
        .sum();
  }

}
