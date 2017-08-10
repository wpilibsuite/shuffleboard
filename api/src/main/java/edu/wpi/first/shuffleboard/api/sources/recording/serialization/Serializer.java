package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

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
