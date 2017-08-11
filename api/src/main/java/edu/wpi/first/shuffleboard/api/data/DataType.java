package edu.wpi.first.shuffleboard.api.data;

import java.util.Objects;

/**
 * Represents types of data that sources can provide and widgets can display. Generally, each subclass
 * should be a singleton or only have a single instance defined. This acts as a pseudo-enum that can
 * be added to.
 *
 * @param <T> the type of the data
 *
 * @see DataTypes
 */
public abstract class DataType<T> {

  private final String name;
  private final Class<T> javaClass;

  protected DataType(String name, Class<T> javaClass) {
    this.name = name;
    this.javaClass = javaClass;
  }

  /**
   * Gets the name of this data type.
   */
  public final String getName() {
    return name;
  }

  public final Class<T> getJavaClass() {
    return javaClass;
  }

  /**
   * Gets the default value of this data type, eg 0 for numbers or an empty String for text.
   */
  public abstract T getDefaultValue();

  /**
   * Checks if this data type is complex or not. This is class-intrinsic.
   */
  public abstract boolean isComplex();

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    DataType that = (DataType) obj;
    return this.getName().equals(that.getName())
        && Objects.equals(this.getJavaClass(), that.getJavaClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return getName();
  }

}
