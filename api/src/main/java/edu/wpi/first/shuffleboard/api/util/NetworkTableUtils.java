package edu.wpi.first.shuffleboard.api.util;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for working with network tables.
 */
public final class NetworkTableUtils {

  /**
   * The root network table.
   */
  public static final ITable rootTable = NetworkTable.getTable("");

  private NetworkTableUtils() {
  }

  @FunctionalInterface
  public interface ITableListenerEx {
    void valueChangedEx(ITable source, String key, Object value, int flags);
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
   * has a hierarchy of "/foo", "/foo/bar", and "/foo/bar/baz".
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
   * @param flag  the flag to check (eg {@link ITable#NOTIFY_DELETE})
   *
   * @return true if the flags match, false otherwise
   */
  public static boolean flagMatches(int flags, int flag) {
    return (flags & flag) != 0;
  }

  /**
   * Checks if the given network table flags contains the {@link ITable#NOTIFY_DELETE delete flag}.
   *
   * <p>This is equivalent to {@code flagMatches(flags, ITable.NOTIFY_DELETE)}
   *
   * @see #flagMatches(int, int)
   */
  public static boolean isDelete(int flags) {
    return flagMatches(flags, ITable.NOTIFY_DELETE);
  }

  /**
   * Gets the data type most closely associated with the value of the given network table key.
   *
   * @param key the network table key to get the data type for
   *
   * @return the data type most closely associated with the given key, or {@link DataTypes#Unknown}
   *         if there is no network table value for the given key
   */
  public static DataType dataTypeForEntry(String key) {
    String normalKey = normalizeKey(key, false);
    if (normalKey.isEmpty() || "/".equals(normalKey)) {
      return DataTypes.Map;
    }
    if (rootTable.containsKey(normalKey)) {
      return DataType.forJavaType(rootTable.getValue(normalKey, null).getClass());
    }
    if (rootTable.containsSubTable(normalKey)) {
      ITable table = rootTable.getSubTable(normalKey);
      String type = table.getString("~TYPE~", table.getString(".type", null));
      if (type == null) {
        return DataTypes.Map;
      } else {
        return DataType.forName(type);
      }
    }
    return DataTypes.Unknown;
  }

  /**
   * Waits for ntcore listeners to be fired. This is a <i>blocking operation</i>.
   */
  public static void waitForNtcoreEvents() {
    CompletableFuture<?> future = new CompletableFuture<>();
    NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
      future.complete(null);
      NetworkTablesJNI.removeEntryListener(uid);
    }, 0xFF);
    future.join();
  }


  /**
   * Shuts down the network table client or server, then clears all entries from network tables.
   * This should be used when changing from server mode to client mode, or changing server
   * address while in client mode.
   */
  public static void shutdown() {
    NetworkTablesJNI.stopDSClient();
    NetworkTablesJNI.stopClient();
    NetworkTablesJNI.stopServer();
    NetworkTablesJNI.deleteAllEntries(); // delete AFTER shutting down the server/client
    NetworkTable.shutdown();
  }

  /**
   * Sets ntcore to server mode.
   *
   * @param port the port on the local machine to run the ntcore server on
   */
  public static void setServer(int port) {
    shutdown();
    NetworkTablesJNI.startServer("networktables.ini", "", port);
    NetworkTable.initialize();
  }

  /**
   * Sets ntcore to client mode.
   *
   * @param serverIp   the ip of the server to connect to, eg "127.0.0.1" or "localhost"
   * @param serverPort the port of the server to connect to. This is normally 1735.
   */
  public static void setClient(String serverIp, int serverPort) {
    shutdown();
    NetworkTablesJNI.startClient(serverIp, serverPort);
    NetworkTable.initialize();
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
   * Creates an ITableListener that wraps an extended table listener to make it usable in the ntcore API.
   *
   * @param extendedListener the listener to wrap
   */
  public static ITableListener createListenerEx(ITableListenerEx extendedListener) {
    return new ITableListener() {
      @Override
      public void valueChanged(ITable source, String key, Object value, boolean isNew) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void valueChangedEx(ITable source, String key, Object value, int flags) {
        extendedListener.valueChangedEx(source, key, value, flags);
      }
    };
  }

}
