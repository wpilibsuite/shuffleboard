package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

public abstract class TypeAdapter<T> implements Serializer<T>, Deserializer<T> {

  private final DataType<T> dataType;

  protected TypeAdapter(DataType<T> dataType) {
    this.dataType = dataType;
  }

  @Override
  public final DataType<T> getDataType() {
    return dataType;
  }

  /**
   * Resets the state of this type adapter (if it has state) before a new recording starts.
   */
  public void cleanUp() { //NOPMD empty abstract method body
    // Default to NOP
  }

}
