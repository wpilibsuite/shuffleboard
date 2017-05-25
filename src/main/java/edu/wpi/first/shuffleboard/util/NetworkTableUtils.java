package edu.wpi.first.shuffleboard.util;

/**
 * Utility class for working with network tables.
 */
public final class NetworkTableUtils {

  private NetworkTableUtils() {
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
   * Normalizes a network table key to start with exactly one leading slash ("/").
   */
  public static String normalizeKey(String key) {
    String normalized = key.replaceAll("/{2,}", "/");
    if (normalized.charAt(0) != '/') {
      normalized = "/" + normalized; //NOPMD
    }
    return normalized;
  }

}
