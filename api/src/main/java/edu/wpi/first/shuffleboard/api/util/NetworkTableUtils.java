package edu.wpi.first.shuffleboard.api.util;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;

import java.util.ArrayList;
import java.util.List;
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
   * Gets the simple representation of a key. For example, "/foo/bar" becomes "bar".
   */
  public static String simpleKey(String key) {
    if (!key.contains("/")) {
      return key;
    }
    return key.substring(key.lastIndexOf('/') + 1);
  }

  /**
   * Normalizes an network table key to contain no consecutive slashes and optionally start
   * with a leading slash. For example:
   *
   * <pre><code>
   * normalizeKey("/foo/bar", true)  == "/foo/bar"
   * normalizeKey("foo/bar", true)   == "/foo/bar"
   * normalizeKey("/foo/bar", false) == "foo/bar"
   * normalizeKey("foo//bar", false) == "foo/bar"
   * </code></pre>
   *
   * @param key              the key to normalize
   * @param withLeadingSlash whether or not the normalized key should begin with a leading slash
   */
  public static String normalizeKey(String key, boolean withLeadingSlash) {
    String normalized = '/' + key;
    normalized = normalized.replaceAll("/{2,}", "/");

    if (!withLeadingSlash && normalized.charAt(0) == '/') {
      // remove leading slash, if present
      normalized = normalized.substring(1);
    }
    return normalized;
  }

  /**
   * Normalizes a network table key to start with exactly one leading slash ("/") and contain no
   * consecutive slashes. For example, {@code "//foo/bar/"} becomes {@code "/foo/bar/"} and
   * {@code "///a/b/c"} becomes {@code "/a/b/c"}.
   *
   * <p>This is equivalent to {@code normalizeKey(key, true)}
   */
  public static String normalizeKey(String key) {
    return normalizeKey(key, true);
  }

  /**
   * Gets a list of the names of all the super tables of a given key. For example, the key "/foo/bar/baz"
   * has a hierarchy of "/", "/foo", "/foo/bar", and "/foo/bar/baz".
   */
  public static List<String> getHierarchy(String key) {
    final String normal = normalizeKey(key, true);
    List<String> hierarchy = new ArrayList<>();
    hierarchy.add("/");
    for (int i = 1; i < normal.length(); i++) {
      if (normal.charAt(i) == NetworkTable.PATH_SEPARATOR) {
        hierarchy.add(normal.substring(0, i));
      } else if (!normal.substring(i, normal.length()).contains("/")) {
        // Now it's the full key
        hierarchy.add(normal);
        break;
      }
    }
    return hierarchy;
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
    String normalKey = normalizeKey(key, false);
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
    return normalizeKey(builder.toString(), true);
  }

  /**
   * Determines the type of the value object and calls the corresponding NetworkTableEntry set
   * method.
   *
   * @param entry the entry to modify
   * @param value the value to which to set the entry
   */
  public static <T> void setEntryValue(NetworkTableEntry entry, T value) throws IllegalArgumentException {
    if (value instanceof Boolean) {
      entry.setBoolean((Boolean)value);
    } else if (value instanceof Number) {
      entry.setDouble(((Number)value).doubleValue());
    } else if (value instanceof String) {
      entry.setString((String)value);
    } else if (value instanceof byte[]) {
      entry.setRaw((byte[])value);
    } else if (value instanceof boolean[]) {
      entry.setBooleanArray((boolean[])value);
    } else if (value instanceof double[]) {
      entry.setNumberArray((Number[])value);
    } else if (value instanceof Boolean[]) {
      entry.setBooleanArray((Boolean[])value);
    } else if (value instanceof Number[]) {
      entry.setNumberArray((Number[])value);
    } else if (value instanceof String[]) {
      entry.setStringArray((String[])value);
    } else if (value instanceof NetworkTableValue) {
      entry.setValue((NetworkTableValue)value);
    } else {
      throw new IllegalArgumentException("Value of type " + value.getClass().getName()
        + " cannot be put into a table");
    }
  }
}
