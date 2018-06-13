package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

/**
 * An interface for functions that can convert raw byte arrays of {@link Serializer serialized} data back to objects.
 *
 * @param <T> the type of data that can be deserialized
 */
public interface Deserializer<T> {

  /**
   * Gets the type of data this can deserialize.
   */
  DataType<T> getDataType();

  /**
   * Deserializes data from a byte buffer, beginning at the given position.
   *
   * @param buffer         the byte buffer to deserialize from
   * @param bufferPosition the position in the buffer to start deserializing from
   */
  T deserialize(byte[] buffer, int bufferPosition);

  /**
   * Gets the size of a byte array that would encode the given value.
   */
  int getSerializedSize(T value);

}
