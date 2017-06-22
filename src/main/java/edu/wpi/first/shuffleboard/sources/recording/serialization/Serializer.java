package edu.wpi.first.shuffleboard.sources.recording.serialization;

import edu.wpi.first.shuffleboard.data.DataType;

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

}
