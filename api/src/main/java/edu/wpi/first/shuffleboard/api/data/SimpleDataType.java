package edu.wpi.first.shuffleboard.api.data;

public abstract class SimpleDataType<T> extends DataType<T> {

  protected SimpleDataType(String name, Class<T> javaClass) {
    super(name, javaClass);
  }

  @Override
  public final boolean isComplex() {
    return false;
  }

}
