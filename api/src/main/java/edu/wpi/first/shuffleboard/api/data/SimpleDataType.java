package edu.wpi.first.shuffleboard.api.data;

public interface SimpleDataType<T> extends DataType<T> {

  @Override
  default boolean isComplex() {
    return false;
  }

}
