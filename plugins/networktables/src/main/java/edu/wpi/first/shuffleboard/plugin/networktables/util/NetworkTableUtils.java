package edu.wpi.first.shuffleboard.plugin.networktables.util;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.util.BitUtils;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
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
   * Checks if network table flags contains a specific flag.
   *
   * @param flags the network table flags
   * @param flag  the flag to check (eg {@link EntryListenerFlags#kDelete})
   *
   * @return true if the flags match, false otherwise
   * @deprecated use {@link BitUtils#flagMatches(int, int)} instead
   */
  @Deprecated
  public static boolean flagMatches(int flags, int flag) {
    return BitUtils.flagMatches(flags, flag);
  }

  /**
   * Checks if the given network table flags contains the {@link EntryListenerFlags#kDelete delete flag}.
   *
   * <p>This is equivalent to {@code flagMatches(flags, EntryListenerFlags.kDelete)}
   *
   * @see BitUtils#flagMatches(int, int)
   */
  public static boolean isDelete(int flags) {
    return BitUtils.flagMatches(flags, EntryListenerFlags.kDelete);
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
}
