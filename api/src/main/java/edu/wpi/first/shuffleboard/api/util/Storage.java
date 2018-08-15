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
   * The root dashboard storage directory.
   */
  private static final String STORAGE_DIR = SystemProperties.USER_HOME + "/Shuffleboard";

  private static final String RECORDING_DIR = STORAGE_DIR + "/recordings";

  private static final String THEMES_DIR = STORAGE_DIR + "/themes";

  private static final String BACKUPS_DIR = STORAGE_DIR + "/backups";

  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
  private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH.mm.ss", Locale.getDefault());

  /**
   * The path to the plugins directory. This directory is scanned once at startup for plugin jars to load.
   */
  private static final String PLUGINS_DIR = STORAGE_DIR + "/plugins";

  private static final String PLUGIN_CACHE_FILE = PLUGINS_DIR + "/.plugincache";

  /**
   * The file extension for data recordings.
   */
  private static final String RECORDING_FILE_EXTENSION = ".sbr";

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
   * The path to the file that contains a cache of external plugin jars.
   * @throws IOException if the file does not exist and creating it fails
   */
  public static Path getPluginCache() throws IOException {
    Path path = Paths.get(PLUGIN_CACHE_FILE);
    if (Files.notExists(path)) {
      Files.createFile(path);
    }
    return path;
  }

  /**
   * The directory that shuffleboard backups are stored in.
   * @throws IOException if creating the directory fails
   */
  public static Path getBackupsDir() throws IOException {
    return findOrCreate(BACKUPS_DIR);
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
    return createRecordingFilePath(startTime, "recording-${time}");
  }

  /**
   * Generates a path to a recording file based on when a recording started. The generated path will always be in the
   * directory {@code /Shuffleboard/recordings/<date>}, where {@code date} is the date formatted by the ISO-8601 format.
   * The recording file name will be the parsed output of the {@code fileNameFormat} parameter; this format supports the
   * following variables to be injected:
   *
   * <table>
   * <tr><th>String</th><th>Value</th></tr>
   * <tr><td>{@code ${date}}</td><td>The ISO-8601 formatted string for the date of the {@code startTime}</td></tr>
   * <tr><td>{@code ${time}}</td><td>The time of the {@code startTime} in a "HH.mm.ss" format</td></tr>
   * </table>
   *
   * <p>For example, a file name format of {@code "practice-match-${time}"} results in paths such as
   * {@code /Shuffleboard/recordings/2019-03-16/practice-match-13.05.15.sbr}.</p>
   * <br>
   * The default file name format is {@code "recording-${time}"}
   * <p>Users are <b>strongly</b> encouraged to use the {@code ${time}} variable to make sure that recording files
   * have unique names, or otherwise set a new file name format every time recording starts.</p>
   *
   * @param startTime      the time the recording started
   * @param fileNameFormat a custom format for the name of the recording file
   */
  public static Path createRecordingFilePath(Instant startTime, String fileNameFormat) throws IOException {
    String date = dateFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));
    String time = timeFormatter.format(LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()));

    String filePathFormat = RECORDING_DIR + "/${date}/" + fileNameFormat + RECORDING_FILE_EXTENSION;
    Path file = Paths.get(filePathFormat
        .replace("${date}", date)
        .replace("${time}", time));

    Path parent = file.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }

    return file;
  }

}
