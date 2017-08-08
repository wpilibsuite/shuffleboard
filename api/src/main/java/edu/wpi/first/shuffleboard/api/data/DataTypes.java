package edu.wpi.first.shuffleboard.api.data;

import edu.wpi.first.shuffleboard.api.data.types.AllType;
import edu.wpi.first.shuffleboard.api.data.types.AnalogInputType;
import edu.wpi.first.shuffleboard.api.data.types.BooleanArrayType;
import edu.wpi.first.shuffleboard.api.data.types.BooleanType;
import edu.wpi.first.shuffleboard.api.data.types.EncoderType;
import edu.wpi.first.shuffleboard.api.data.types.MapType;
import edu.wpi.first.shuffleboard.api.data.types.NoneType;
import edu.wpi.first.shuffleboard.api.data.types.NumberArrayType;
import edu.wpi.first.shuffleboard.api.data.types.NumberType;
import edu.wpi.first.shuffleboard.api.data.types.RawByteType;
import edu.wpi.first.shuffleboard.api.data.types.SendableChooserType;
import edu.wpi.first.shuffleboard.api.data.types.SpeedControllerType;
import edu.wpi.first.shuffleboard.api.data.types.StringArrayType;
import edu.wpi.first.shuffleboard.api.data.types.StringType;
import edu.wpi.first.shuffleboard.api.data.types.UnknownType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class DataTypes {

  // Catchall or wildcard types
  public static final DataType<?> None = new NoneType();
  public static final DataType<?> All = new AllType();
  public static final DataType<?> Unknown = new UnknownType();

  // Simple types
  public static final DataType<Number> Number = new NumberType();
  public static final DataType<String> String = new StringType();
  public static final DataType<Boolean> Boolean = new BooleanType();
  public static final DataType<double[]> NumberArray = new NumberArrayType();
  public static final DataType<String[]> StringArray = new StringArrayType();
  public static final DataType<boolean[]> BooleanArray = new BooleanArrayType();
  public static final DataType<byte[]> RawBytes = new RawByteType();

  // Complex types
  public static final ComplexDataType<MapData> Map = new MapType();
  public static final ComplexDataType<SendableChooserData> SendableChooser = new SendableChooserType();
  public static final ComplexDataType<AnalogInputData> AnalogInput = new AnalogInputType();
  public static final ComplexDataType<SpeedControllerData> SpeedController = new SpeedControllerType();
  public static final ComplexDataType<EncoderData> Encoder = new EncoderType();

  private static final Map<String, DataType> dataTypes = new TreeMap<>();

  // Register all builtin types
  static {
    register(All);
    register(None);
    register(Unknown);
    register(Number);
    register(String);
    register(Boolean);
    register(NumberArray);
    register(StringArray);
    register(BooleanArray);
    register(Map);
    register(SendableChooser);
    register(AnalogInput);
    register(SpeedController);
    register(Encoder);
  }

  private DataTypes() {
  }

  /**
   * Registers the given data type.
   *
   * @param dataType the data type to register
   */
  public static void register(DataType<?> dataType) {
    dataTypes.put(dataType.getName(), dataType);
  }

  /**
   * Gets the data type with the given name.
   */
  public static Optional<DataType<?>> forName(String name) {
    return Optional.ofNullable(dataTypes.get(name));
  }

  /**
   * Gets the registered data type of the given class.
   */
  @SuppressWarnings("unchecked")
  public static <D extends DataType> Optional<D> forType(Class<D> clazz) {
    return (Optional<D>) dataTypes.values()
        .stream()
        .filter(d -> d.getClass() == clazz)
        .findFirst();
  }

  /**
   * Gets the registered data types of the given types.
   */
  public static Set<DataType> forTypes(Class<? extends DataType>... types) {
    return Arrays.stream(types)
        .map(DataTypes::forType)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

}
