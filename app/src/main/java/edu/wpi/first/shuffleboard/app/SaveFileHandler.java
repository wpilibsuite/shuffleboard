package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.app.json.JsonBuilder;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.FileChooser;

/**
 * Helper class for saving and loading Shuffleboard save files.
 */
public final class SaveFileHandler {

  private static final Logger log = Logger.getLogger(SaveFileHandler.class.getName());

  private File currentFile = null;

  /**
   * Saves dashboard data. If no save file has been specified, then the user will be prompted to choose a save file.
   *
   * @param data the data to save
   *
   * @throws IOException if the data could not be saved
   */
  public void save(DashboardData data) throws IOException {
    if (currentFile == null) {
      saveAs(data);
    } else {
      saveFile(currentFile, data);
    }
  }

  /**
   * Saves dashboard data, prompting the user to specify the file to save to.
   *
   * @param data the data to save
   *
   * @throws IOException if the data could not be saved
   */
  public void saveAs(DashboardData data) throws IOException {
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Shuffleboard Save File (.json)", "*.json"));
    if (currentFile == null) {
      chooser.setInitialDirectory(Storage.getStorageDir());
      chooser.setInitialFileName("shuffleboard.json");
    } else {
      chooser.setInitialDirectory(currentFile.getAbsoluteFile().getParentFile());
      chooser.setInitialFileName(currentFile.getName());
    }

    Optional.ofNullable(chooser.showSaveDialog(null))
        .ifPresent(file -> saveFile(file, data));
  }

  private void saveFile(File file, DashboardData data) {
    try {
      Writer writer = Files.newWriter(file, Charset.forName("UTF-8"));

      JsonBuilder.forSaveFile().toJson(data, writer);
      writer.flush();
    } catch (IOException | RuntimeException e) {
      log.log(Level.WARNING, "Couldn't save", e);
      return;
    }

    currentFile = file;
    AppPreferences.getInstance().setSaveFile(currentFile);
  }

  /**
   * Prompts the user to choose a save file to load, then returns the contents. Returns null if no file was selected.
   *
   * @return the data in the file the user selected, or null if the user cancelled the load
   *
   * @throws IOException if the user-selected file could not be read
   */
  public DashboardData load() throws IOException {
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(Storage.getStorageDir());
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Shuffleboard Save File (.json)", "*.json"));

    final File selected = chooser.showOpenDialog(null);

    return load(selected);
  }

  /**
   * Loads dashboard data from a specific file.
   *
   * @param file the file to aod data from
   *
   * @return the data stored in the file
   *
   * @throws IOException if the file could not be read
   */
  public DashboardData load(File file) throws IOException {
    if (file == null) {
      return null;
    }
    Reader reader = Files.newReader(file, Charset.forName("UTF-8"));

    DashboardData dashboardData = JsonBuilder.forSaveFile().fromJson(reader, DashboardData.class);
    if (dashboardData == null) {
      throw new IOException("Save file could not be read: " + file);
    }
    currentFile = file;
    AppPreferences.getInstance().setSaveFile(file);
    return dashboardData;
  }

  /**
   * Clears the current file status.
   */
  public void clear() {
    currentFile = null;
  }

}
