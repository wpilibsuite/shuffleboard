package edu.wpi.first.shuffleboard.api.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

  public static final String RECORDING_DIR = STORAGE_DIR + "/recordings";

  public static final String RECORDING_FILE_FORMAT = RECORDING_DIR + "/${date}/recording-${time}.sbr";

  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
  private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH.mm.ss", Locale.getDefault());

  /**
   * The path to the plugins directory. This directory is scanned once at startup for plugin jars to load.
   */
  public static final String PLUGINS_DIR = STORAGE_DIR + "/plugins";

  private Storage() {
  }

  /**
   * Generates the path to a recording file based on when a recording started. The generated path is in the format
   * {@code /SmartDashboard/recordings/<date>/recording-<time>.sbr}, where {@code date} is the date formatted by the
   * ISO-8601 format, and {@code time} is a modified version that uses periods ({@code "."}) instead of colons because
   * Windows does not allow colon characters in file names.
   *
   * @param startTime the time the recording started
   */
  public static String createRecordingFilePath(Instant startTime) {
    String date = dateFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));
    String time = timeFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));
    return RECORDING_FILE_FORMAT
        .replace("${date}", date)
        .replace("${time}", time);
  }

}
