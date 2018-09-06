package edu.wpi.first.shuffleboard.api.sources;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class DataSourceUtils {

  private static final Pattern oldMetadataPattern = Pattern.compile("(^|/)~\\w+~($|/)");
  private static final Pattern newMetadataPattern = Pattern.compile("(^|/)\\.");

  private DataSourceUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Checks if the given source URI is metadata, eg matches the format "~METADATA~" or ".metadata", or has any parent
   * that matches either of those formats.
   */
  public static boolean isMetadata(String sourceUri) {
    return newMetadataPattern.matcher(sourceUri).find() // Check new metadata first, since that's most likely
        || oldMetadataPattern.matcher(sourceUri).find();
  }

  /**
   * Checks if the given source URI is not metadata, eg does not match either "~METADATA~" or ".metadata".
   * <br>Shorthand for {@code !isMetadata(sourceUri)}.
   *
   * @see #isMetadata(String)
   */
  public static boolean isNotMetadata(String sourceUri) {
    return !isMetadata(sourceUri);
  }

  /**
   * Get the hierarchy of a source path without a type specifier, eg "/foo/bar/baz" will return
   * ["/", "/foo", "/foo/bar", "/foo/bar/baz"].
   *
   * @param sourcePath the source path to get the hierachy of
   *
   * @return the hierarchy of a source path
   */
  public static List<String> getHierarchy(String sourcePath) {
    final String normal = sourcePath.replaceAll("/{2,}", "/");
    List<String> hierarchy = new ArrayList<>();
    if (normal.length() == 1) {
      hierarchy.add(normal);
      return hierarchy;
    }
    for (int i = 1; ; i = normal.indexOf('/', i + 1)) {
      if (i == -1) {
        // add the full key
        hierarchy.add(normal);
        break;
      } else {
        hierarchy.add(normal.substring(0, i));
      }
    }
    return hierarchy;
  }

  /**
   * Gets the base name of a data source, eg "/foo/bar" -> "bar".
   *
   * @param sourcePath the source path to get the base name for
   *
   * @return the base name of a source path
   */
  public static String baseName(String sourcePath) {
    if (sourcePath.contains("/")) {
      return sourcePath.substring(sourcePath.lastIndexOf('/') + 1);
    } else {
      return sourcePath;
    }
  }

}
