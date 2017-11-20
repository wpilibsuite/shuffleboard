package edu.wpi.first.shuffleboard.api.util;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.regex.Pattern;

/**
 * Utility class for working with network tables.
 */
public final class NetworkTableUtils {

  /**
   * The root network table.
   */
  public static final NetworkTableInstance inst = NetworkTableInstance.getDefault();
  public static final NetworkTable rootTable = inst.getTable("");

  private static final Pattern oldMetadataPattern = Pattern.compile("(^|/)~\\w+~($|/)");
  private static final Pattern newMetadataPattern = Pattern.compile("(^|/)\\.");

  private NetworkTableUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Checks if network table flags contains a specific flag.
   *
   * @param flags the network table flags
   * @param flag  the flag to check (eg {@link EntryListenerFlags#kDelete})
   *
   * @return true if the flags match, false otherwise
   */
  public static boolean flagMatches(int flags, int flag) {
    return (flags & flag) != 0;
  }

  /**
   * Checks if the given network table flags contains the {@link EntryListenerFlags#kDelete delete flag}.
   *
   * <p>This is equivalent to {@code flagMatches(flags, EntryListenerFlags.kDelete)}
   *
   * @see #flagMatches(int, int)
   */
  public static boolean isDelete(int flags) {
    return flagMatches(flags, EntryListenerFlags.kDelete);
  }

  /**
   * Checks if the given key is metadata, eg matches the format "~METADATA~" or ".metadata"
   */
  public static boolean isMetadata(String key) {
    return oldMetadataPattern.matcher(key).find()
        || newMetadataPattern.matcher(key).find();
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
      return DataTypes.getDefault().forJavaType(rootTable.getEntry(normalKey).getValue().getValue().getClass()).get();
    }
    if (rootTable.containsSubTable(normalKey)) {
      NetworkTable table = rootTable.getSubTable(normalKey);
      String type = table.getEntry("~TYPE~").getString(table.getEntry(".type").getString(null));
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
    // Wait for the network mode to be zero (everything off)
    while (instance.getNetworkMode() != 0) { // NOPMD empty 'while' statement
      // busy wait
    }
    // delete ALL entries, including persistent ones (deleteAllEntries skips persistent entries)
    for (NetworkTableEntry entry : instance.getEntries("", 0)) {
      entry.delete();
    }
  }

  /**
   * Sets ntcore to server mode.
   *
   * @param port the port on the local machine to run the ntcore server on
   */
  public static void setServer(int port) {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    shutdown(inst);
    inst.startServer("networktables.ini", "", port);
  }

  /**
   * Sets ntcore to client mode.
   *
   * @param serverIp   the ip of the server to connect to, eg "127.0.0.1" or "localhost"
   * @param serverPort the port of the server to connect to. This is normally 1735.
   */
  public static void setClient(String serverIp, int serverPort) {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    shutdown(inst);
    inst.startClient(serverIp, serverPort);
  }

  /**
   * Concatenates multiple keys.
   *
   * @param key1 the first key
   * @param key2 the second key
   * @param more optional extra keys to concatenate
   */
  public static String concat(String key1, String key2, String... more) {
    StringBuilder builder = new StringBuilder(key1).append('/').append(key2);
    for (String s : more) {
      builder.append('/').append(s);
    }
    return NetworkTable.normalizeKey(builder.toString(), true);
  }
}
