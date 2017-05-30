package edu.wpi.first.shuffleboard.widget;

import java.util.stream.Stream;

/** An enum representing all the possible types of data that widgets can handle. */
public enum DataType {

  // Wildcard types

  /** An unknown data type. Equivalent to null. */
  Unknown,

  /** Matches all known data types. */
  All,

  // Single key-value types

  /** Data type for a text string. */
  String,

  /** Data type for a single numeric value. */
  Number,

  /** Data type for a single boolean value. */
  Boolean,

  /** Data type for an array of strings. */
  StringArray,

  /** Data type for an array of numbers. */
  NumberArray,

  /** Data type for an array of booleans. */
  BooleanArray,

  /**
   * Data type for an array of bytes that can be deserialized to some arbitrary type by a consumer.
   */
  RawBytes,

  // Composite types

  /**
   * A generic data type encompassing all composite types (ie types with more than a single value).
   */
  Map,

  RobotDrive,

  MotorController,

  Sensor,

  DigitalInput,

  AnalogInput,

  Gyro,

  Encoder,

  Command,

  SendableChooser,

  CameraServerCamera;

  private final String name;

  DataType() {
    this.name = name();
  }

  DataType(String name) {
    this.name = name;
  }

  /**
   * Gets the name of this data type.
   *
   * <p><i>Not to be confused with {@link #name()}, which gets the name of the enum constant.</i>
   */
  public String getName() {
    return name;
  }

  /** Gets the data type with the given name, or {@link #Unknown} if no such data type exists. */
  public static DataType forName(String name) {
    return Stream.of(values())
        .filter(dataType -> dataType.name.equals(name))
        .findFirst()
        .orElse(Unknown);
  }

  /** Gets the data type most closely associated with the given class. */
  public static DataType valueOf(Class<?> type) {
    if (type == String.class) {
      return String;
    } else if (Number.class.isAssignableFrom(type)
        || type == double.class
        || type == int.class
        || type == long.class) {
      return Number;
    } else if (type == Boolean.class || type == boolean.class) {
      return Boolean;
    } else if (type == String[].class) {
      return StringArray;
    } else if (type == double[].class || type == Double[].class) {
      return NumberArray;
    } else if (type == boolean[].class || type == Boolean[].class) {
      return BooleanArray;
    } else if (type == byte[].class || type == Byte[].class) {
      return RawBytes;
    } else {
      return Unknown;
    }
  }
}
