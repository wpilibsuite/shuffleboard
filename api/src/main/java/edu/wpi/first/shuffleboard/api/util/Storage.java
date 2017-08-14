package edu.wpi.first.shuffleboard.api.util;

/**
 * Utilities for local file storage.
 */
public final class Storage {

  /**
   * The user home directory.
   */
  public static final String USER_HOME = System.getProperty("user.home");

  /**
   * The root dashboard storage directory.
   */
  public static final String STORAGE_DIR = USER_HOME + "/SmartDashboard";

  public static final String RECORDING_FILE_FORMAT = STORAGE_DIR + "/dashboard_recording_%s.frc";

  public static final String DEFAULT_RECORDING_FILE = STORAGE_DIR + "/default_dashboard_recording.frc";

  /**
   * The path to the plugins directory. This directory is scanned once at startup for plugin jars to load.
   */
  public static final String PLUGINS_DIR = STORAGE_DIR + "/plugins";

  private Storage() {
  }

}
