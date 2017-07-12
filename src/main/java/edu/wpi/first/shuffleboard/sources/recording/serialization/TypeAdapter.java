package edu.wpi.first.shuffleboard.sources.recording.serialization;

import edu.wpi.first.shuffleboard.data.DataType;

public abstract class TypeAdapter<T> implements Serializer<T>, Deserializer<T> {

  private final DataType<T> dataType;

  protected TypeAdapter(DataType<T> dataType) {
    this.dataType = dataType;
  }

  @Override
  public final DataType<T> getDataType() {
    return dataType;
  }

}
