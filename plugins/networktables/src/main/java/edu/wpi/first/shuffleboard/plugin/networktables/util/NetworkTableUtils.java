package edu.wpi.first.shuffleboard.plugin.networktables.util;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;

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

  public static DataType dataTypeForTypeString(String typeString) {
    if ("boolean".equals(typeString)) {
      return DataTypes.Boolean;
    } else if ("double".equals(typeString) || "int".equals(typeString) || "float".equals(typeString)) {
      return DataTypes.Number;
    } else if ("string".equals(typeString) || "json".equals(typeString)) {
      return DataTypes.String;
    } else if ("raw".equals(typeString)) {
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
    String normalKey = NetworkTable.normalizeKey(key, false);
    if (normalKey.isEmpty() || "/".equals(normalKey)) {
      return DataTypes.Map;
    }
    if (rootTable.containsKey(normalKey)) {
      return dataTypeForTypeString(rootTable.getTopic(normalKey).getTypeString());
    }
    if (rootTable.containsSubTable(normalKey)) {
      NetworkTable table = rootTable.getSubTable(normalKey);
      String type;
      if (table.containsKey("~TYPE~")) {
        type = table.getEntry("~TYPE~").getString(null);
      } else if (table.containsKey(".type")) {
        type = table.getEntry(".type").getString(null);
      } else {
        return DataTypes.Map;
      }
      if (type == null) {
        return DataTypes.Map;
      } else {
        return DataTypes.getDefault().forName(type)
            .orElse(DataTypes.Map);
      }
    }
    return null;
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
