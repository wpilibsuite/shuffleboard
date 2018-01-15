package edu.wpi.first.shuffleboard.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Utilities for local file storage.
 */
public final class Storage {

  private static final Logger log = Logger.getLogger(Storage.class.getName());

  /**
   * The user home directory.
   */
  private static final String USER_HOME = System.getProperty("user.home");

  /**
   * The root dashboard storage directory.
   */
  private static final String STORAGE_DIR = USER_HOME + "/Shuffleboard";

  private static final String RECORDING_DIR = STORAGE_DIR + "/recordings";

  private static final String THEMES_DIR = STORAGE_DIR + "/themes";

  private static final String RECORDING_FILE_FORMAT = RECORDING_DIR + "/${date}/recording-${time}.sbr";

  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
  private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH.mm.ss", Locale.getDefault());

  /**
   * The path to the plugins directory. This directory is scanned once at startup for plugin jars to load.
   */
  private static final String PLUGINS_DIR = STORAGE_DIR + "/plugins";

  private Storage() {
  }

  private static Path findOrCreate(String directory) throws IOException {
    Path path = Paths.get(directory);
    if (!Files.exists(path)) {
      log.info("Creating directory " + path);
      Files.createDirectories(path);
    }

    return path;
  }

  /**
   * The main storage directory that all Shuffleboard files should exist in.
   * @throws IOException if creating the directory fails
   */
  public static File getStorageDir() throws IOException {
    return findOrCreate(STORAGE_DIR).toFile();
  }

  /**
   * The directory that contains the nested recording files and sub-directories.
   * @throws IOException if creating the directory fails
   */
  public static File getRecordingDir() throws IOException {
    return findOrCreate(RECORDING_DIR).toFile();
  }

  /**
   * The directory that plugins are loaded from.
   * @throws IOException if creating the directory fails
   */
  public static Path getPluginPath() throws IOException {
    return findOrCreate(PLUGINS_DIR);
  }

  /**
   * Gets the directory for custom external themes, creating it if it does not exist.
   * @throws IOException if the directory cannot be created
   */
  public static Path getThemesDir() throws IOException {
    return findOrCreate(THEMES_DIR);
  }

  /**
   * Generates the path to a recording file based on when a recording started. The generated path is in the format
   * {@code /Shuffleboard/recordings/<date>/recording-<time>.sbr}, where {@code date} is the date formatted by the
   * ISO-8601 format, and {@code time} is a modified version that uses periods ({@code "."}) instead of colons because
   * Windows does not allow colon characters in file names.
   *
   * @param startTime the time the recording started
   */
  public static Path createRecordingFilePath(Instant startTime) throws IOException {
    String date = dateFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));
    String time = timeFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));

    Path file = Paths.get(RECORDING_FILE_FORMAT
        .replace("${date}", date)
        .replace("${time}", time));

    Path parent = file.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }

    return file;
  }

}
