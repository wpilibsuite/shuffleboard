package edu.wpi.first.shuffleboard.api.util;

import java.util.Locale;

/**
 * Utility class for detecting the current operating system.
 */
public final class OsDetector {

  private static final OperatingSystemType operatingSystemType;

  private OsDetector() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  static {
    String osName = System.getProperty("os.name").toLowerCase(Locale.US);
    if (osName.contains("windows")) {
      operatingSystemType = OperatingSystemType.WINDOWS;
    } else if (osName.contains("mac")) {
      operatingSystemType = OperatingSystemType.MAC;
    } else {
      operatingSystemType = OperatingSystemType.LINUX;
    }
  }

  public enum OperatingSystemType {
    /**
     * A windows operating system.
     */
    WINDOWS,
    /**
     * OS X or Mac OS.
     */
    MAC,
    /**
     * Generic linux-based operating system.
     */
    LINUX
  }

  /**
   * Gets the type of the operating system.
   */
  public static OperatingSystemType getOperatingSystemType() {
    return operatingSystemType;
  }

}
