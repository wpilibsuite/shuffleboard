package edu.wpi.first.shuffleboard.data;

/**
 * Represents types of data that sources can provide and widgets can display. Generally, each subclass
 * should be a singleton or only have a single instance defined. This acts as a pseudo-enum that can
 * be added to.
 *
 * @param <T> the type of the data
 *
 * @see DataTypes
 */
public interface DataType<T> {

  /**
   * Gets the name of this data type.
   */
  String getName();

  /**
   * Gets the default value of this data type, eg 0 for numbers or an empty String for text.
   */
  T getDefaultValue();

  /**
   * Checks if this data type is complex or not. This is class-intrinsic.
   */
  boolean isComplex();

  /**
   * Gets the data type most closely associated with the given Java type.
   */
  static DataType<?> valueOf(Class<?> type) {
    if (type == String.class) {
      return DataTypes.String;
    } else if (Number.class.isAssignableFrom(type)
        || type == double.class || type == int.class || type == long.class) {
      return DataTypes.Number;
    } else if (type == Boolean.class || type == boolean.class) {
      return DataTypes.Boolean;
    } else if (type == String[].class) {
      return DataTypes.StringArray;
    } else if (type == double[].class || type == Double[].class) {
      return DataTypes.NumberArray;
    } else if (type == boolean[].class || type == Boolean[].class) {
      return DataTypes.BooleanArray;
    } else if (type == byte[].class || type == Byte[].class) {
      return DataTypes.RawBytes;
    } else {
      return DataTypes.Unknown;
    }
  }

  /**
   * Gets the data type with the given name.
   */
  static DataType<?> forName(String name) {
    return DataTypes.forName(name).orElse(DataTypes.Unknown);
  }

}
