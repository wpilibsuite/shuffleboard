package edu.wpi.first.shuffleboard.data;

public interface SimpleDataType<T> extends DataType<T> {

  @Override
  default boolean isComplex() {
    return false;
  }

}
