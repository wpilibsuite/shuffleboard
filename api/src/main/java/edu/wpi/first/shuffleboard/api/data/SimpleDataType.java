package edu.wpi.first.shuffleboard.api.data;

/**
 * Represents the type of simple data such as numbers or strings.
 *
 * @param <T> the type of the data
 */
public abstract class SimpleDataType<T> extends DataType<T> {

  protected SimpleDataType(String name, Class<T> javaClass) {
    super(name, javaClass);
  }

  @Override
  public final boolean isComplex() {
    return false;
  }

}
