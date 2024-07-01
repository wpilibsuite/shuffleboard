package edu.wpi.first.shuffleboard.plugin.networktables.util;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.shuffleboard.api.util.StringUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

/**
 * Utility class for working with network tables.
 */
public final class NetworkTableUtils {

  /**
   * The root network table.
   */
  public static final NetworkTableInstance inst = NetworkTableInstance.getDefault();
  public static final NetworkTable rootTable = inst.getTable("");

  private NetworkTableUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Gets the topic name for a particular event. Throws if event is not topic or value event.
   *
   * @param event event
   * @return topic name
   */
  @SuppressWarnings("PMD.ConfusingTernary")
  public static String topicNameForEvent(NetworkTableEvent event) {
    if (event.topicInfo != null) {
      return event.topicInfo.name;
    } else if (event.valueData != null) {
      return event.valueData.getTopic().getName();
    } else {
      throw new IllegalArgumentException("event does not have a name");
    }
  }

  /**
   * Gets the data type associated with the given network table type string.
   *
   * @param typeString the network table type string to get the data type for
   *
   * @return the data type most closely associated with the given type string
   */
  public static DataType dataTypeForTypeString(String typeString) {
    if ("boolean".equals(typeString)) {
      return DataTypes.Boolean;
    } else if ("double".equals(typeString) || "int".equals(typeString) || "float".equals(typeString)) {
      return DataTypes.Number;
    } else if ("string".equals(typeString) || "json".equals(typeString)) {
      return DataTypes.String;
    } else if ("raw".equals(typeString) || "msgpack".equals(typeString) || "protobuf".equals(typeString)) {
      return DataTypes.ByteArray;
    } else if ("boolean[]".equals(typeString)) {
      return DataTypes.BooleanArray;
    } else if ("double[]".equals(typeString) || "int[]".equals(typeString) || "float[]".equals(typeString)) {
      return DataTypes.NumberArray;
    } else if ("string[]".equals(typeString)) {
      return DataTypes.StringArray;
    } else {
      return DataTypes.Unknown;
    }
  }

  /**
   * Gets the data type most closely associated with the value of the given network table key.
   *
   * @param key the network table key to get the data type for
   *
   * @return the data type most closely associated with the given key
   */
  public static DataType dataTypeForEntry(String key) {
    var networkTableSourceType = NetworkTableSourceType.getInstance();
    return networkTableSourceType.dataTypeForSource(DataTypes.getDefault(), networkTableSourceType.toUri(key));
  }

  /**
   * Shuts down the default instance.
   */
  public static void shutdown() {
    shutdown(NetworkTableInstance.getDefault());
  }

  /**
   * Shuts down the network table client or server, then clears all entries from network tables.
   * This should be used when changing from server mode to client mode, or changing server
   * address while in client mode.
   */
  public static void shutdown(NetworkTableInstance instance) {
    instance.stopDSClient();
    instance.stopClient();
    instance.stopServer();
  }
}
