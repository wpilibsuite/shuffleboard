package edu.wpi.first.shuffleboard.plugin.base.recording.serialization;

import com.google.common.primitives.Bytes;

import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringType;

import java.io.UnsupportedEncodingException;

public class StringAdapter extends TypeAdapter<String> {

  public StringAdapter() {
    super(new StringType());
  }

  @Override
  public byte[] serialize(String value) {
    try {
      return Bytes.concat(Serialization.toByteArray(value.length()), value.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF-8 is not supported (the JVM is not to spec!)", e);
    }
  }

  @Override
  public String deserialize(byte[] buffer, int bufferPosition) {
    int cursor = bufferPosition;
    int length = Serialization.readInt(buffer, cursor);
    cursor += Serialization.SIZE_OF_INT;
    if (buffer.length < cursor + length) {
      throw new IllegalArgumentException(String.format(
          "Not enough bytes to read from. String length = %d, starting position = %d, buffer length = %d",
          length, cursor, buffer.length));
    }
    byte[] bytes = Serialization.subArray(buffer, cursor, cursor + length);
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF-8 is not supported (the JVM is not to spec!)", e);
    }
  }

  @Override
  public int getSerializedSize(String value) {
    return Serialization.SIZE_OF_INT + value.length();
  }

}
