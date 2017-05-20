package edu.wpi.first.shuffleboard.widget;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public final class DataType {

  private final String name;

  public String getName() {
    return name;
  }

  // Wildcard types


  /**
   * An unknown data type. Equivalent to null.
   */
  public static final DataType Unknown = new DataType("Unknown");

  /**
   * Matches all known data types.
   */
  public static final DataType All = new DataType("All");


  // Single key-value types


  /**
   * Data type for a text string.
   */
  public static final DataType Text = new DataType("Text");

  /**
   * Data type for a single numeric value.
   */
  public static final DataType Number = new DataType("Number");

  /**
   * Data type for a single boolean value.
   */
  public static final DataType Boolean = new DataType("Boolean");

  /**
   * Data type for an array of strings.
   */
  public static final DataType TextArray = new DataType("TextArray");

  /**
   * Data type for an array of numbers.
   */
  public static final DataType NumberArray = new DataType("NumberArray");

  /**
   * Data type for an array of booleans.
   */
  public static final DataType BooleanArray = new DataType("BooleanArray");

  /**
   * Data type for an array of bytes that can be deserialized to some arbitrary
   * type by a consumer.
   */
  public static final DataType RawBytes = new DataType("RawBytes");


  // Composite types


  /**
   * A generic data type encompassing all composite types (ie types with more than a single value).
   */
  public static final DataType Composite = new DataType("Composite");
  public static final DataType RobotDrive = new DataType("RobotDrive");
  public static final DataType MotorController = new DataType("MotorController");
  public static final DataType Sensor = new DataType("Sensor");
  public static final DataType DigitalInput = new DataType("DigitalInput");
  public static final DataType AnalogInput = new DataType("AnalogInput");
  public static final DataType Gyro = new DataType("Gyro");
  public static final DataType Encoder = new DataType("Encoder");
  public static final DataType Command = new DataType("Command");
  public static final DataType SendableChooser = new DataType("SendableChooser");
  public static final DataType CameraServerCamera = new DataType("CameraServerCamera");

  private DataType(String name) {
    this.name = name;
  }

  private static final Map<String, DataType> fieldConstants;

  static {
    fieldConstants = Arrays.stream(DataType.class.getDeclaredFields())
                           .filter(f -> Modifier.isStatic(f.getModifiers()))
                           .filter(f -> f.getType() == DataType.class)
                           .peek(f -> f.setAccessible(true))
                           .map(DataType::safeGet)
                           .collect(Collectors.toMap(DataType::getName, d -> d));
  }

  private static DataType safeGet(Field f) {
    try {
      return (DataType) f.get(null);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static DataType of(String name) {
    return fieldConstants.computeIfAbsent(name, DataType::new);
  }

  public static DataType valueOf(String name) {
    return fieldConstants.getOrDefault(name, Unknown);
  }

  public static DataType valueOf(Class<?> type) {
    if (type == String.class) {
      return Text;
    } else if (Number.class.isAssignableFrom(type)
        || type == double.class || type == int.class || type == long.class) {
      return Number;
    } else if (type == Boolean.class || type == boolean.class) {
      return Boolean;
    } else if (type == String[].class) {
      return TextArray;
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
