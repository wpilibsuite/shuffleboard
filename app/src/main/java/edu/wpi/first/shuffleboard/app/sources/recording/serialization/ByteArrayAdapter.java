package edu.wpi.first.shuffleboard.app.sources.recording.serialization;

import com.google.common.primitives.Bytes;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.app.sources.recording.Serialization;

import static edu.wpi.first.shuffleboard.app.sources.recording.Serialization.SIZE_OF_INT;

public class ByteArrayAdapter extends TypeAdapter<byte[]> {

  public ByteArrayAdapter() {
    super(DataTypes.RawBytes);
  }

  @Override
  public byte[] serialize(byte[] data) {
    return Bytes.concat(Serialization.toByteArray(data.length), data);
  }

  @Override
  public byte[] deserialize(byte[] buffer, int bufferPosition) {
    int length = Serialization.readInt(buffer, bufferPosition);
    return Serialization.subArray(buffer, bufferPosition + SIZE_OF_INT, bufferPosition + SIZE_OF_INT + length);
  }

  @Override
  public int getSerializedSize(byte[] value) {
    return SIZE_OF_INT + value.length;
  }

}
