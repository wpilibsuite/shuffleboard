package edu.wpi.first.shuffleboard.api.sources;

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

}
