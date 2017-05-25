package edu.wpi.first.shuffleboard.widget;

/**
 * An enum representing all the possible types of data that widgets can handle.
 */
public enum DataType {

  // Wildcard types


  /**
   * An unknown data type. Equivalent to null.
   */
  Unknown("Unknown"),

  /**
   * Matches all known data types.
   */
  All("All"),


  // Single key-value types


  /**
   * Data type for a text string.
   */
  String("String"),

  /**
   * Data type for a single numeric value.
   */
  Number("Number"),

  /**
   * Data type for a single boolean value.
   */
  Boolean("Boolean"),

  /**
   * Data type for an array of strings.
   */
  StringArray("StringArray"),

  /**
   * Data type for an array of numbers.
   */
  NumberArray("NumberArray"),

  /**
   * Data type for an array of booleans.
   */
  BooleanArray("BooleanArray"),

  /**
   * Data type for an array of bytes that can be deserialized to some arbitrary
   * type by a consumer.
   */
  RawBytes("RawBytes"),


  // Composite types


  /**
   * A generic data type encompassing all composite types (ie types with more than a single value).
   */
  Composite("Composite"),

  RobotDrive("RobotDrive"),

  MotorController("MotorController"),

  Sensor("Sensor"),

  DigitalInput("DigitalInput"),

  AnalogInput("AnalogInput"),

  Gyro("Gyro"),

  Encoder("Encoder"),

  Command("Command"),

  SendableChooser("SendableChooser"),

  CameraServerCamera("CameraServerCamera");

  private final String name;

  DataType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * Gets the data type most closely associated with the given class.
   */
  public static DataType valueOf(Class<?> type) {
    if (type == String.class) {
      return String;
    } else if (Number.class.isAssignableFrom(type)
        || type == double.class || type == int.class || type == long.class) {
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
