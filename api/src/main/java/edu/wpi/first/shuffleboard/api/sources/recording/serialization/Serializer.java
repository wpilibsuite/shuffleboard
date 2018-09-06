package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

/**
 * An interface for functions that can convert data of type {@code T} to a raw byte array. This byte array should be
 * able to be read by a {@link Deserializer} to reconstruct the serialized object.
 *
 * @param <T> the type of data that can be serialized
 */
public interface Serializer<T> {

  /**
   * Gets the type of the data this can serialize.
   */
  DataType<T> getDataType();

  /**
   * Serializes the given data as a byte array.
   *
   * @param data the object to serialize
   */
  byte[] serialize(T data);

  /**
   * If this serializer uses external recording files other than the primary Shuffleboard recording file (such as
   * video files for camera streams), this will save those files.
   */
  default void flush() {
    // NOP
  }

}
