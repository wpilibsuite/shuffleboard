package edu.wpi.first.shuffleboard.app.prefs;

import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.util.PreferencesUtils;

import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Contains the user preferences for the app. These preferences are user-specific and are not contained in save files.
 */
public final class AppPreferences {

  private final Property<Theme> theme = new SimpleObjectProperty<>(this, "Theme", Themes.INITIAL_THEME);
  private final DoubleProperty defaultTileSize = new SimpleDoubleProperty(this, "defaultTileSize", 128);
  private final BooleanProperty confirmTabClose =
      new SimpleBooleanProperty(this, "confirmTabClose", true);
  private final Property<File> saveFile = new SimpleObjectProperty<>(this, "saveFile", null);
  private final BooleanProperty autoLoadLastSaveFile =
      new SimpleBooleanProperty(this, "automaticallyLoadLastSaveFile", true);
  private final BooleanProperty confirmExit =
      new SimpleBooleanProperty(this, "showConfirmationDialogWhenExiting", true);

  private final Category settings = Category.of("App Settings",
      Group.of("Theme",
          Setting.of("Theme", "The theme to display Shuffleboard with", theme)
      ),
      Group.of("Tab Settings",
          Setting.of("Default tile size", "The tile size of new tabs (existing tabs are unaffected)", defaultTileSize),
          Setting.of("Confirm tab close", "Requests confirmation when closing a tab", confirmTabClose)
      ),
      Group.of("Startup",
          Setting.of("Load last save file", "Load the most recent save file at startup", autoLoadLastSaveFile)
      ),
      Group.of("Miscellaneous",
          Setting.of("Confirm exit", "Request confirmation before exiting", confirmExit)
      )
  );

  @VisibleForTesting
  static AppPreferences instance = new AppPreferences();

  /**
   * Creates a new app preferences instance.
   */
  public AppPreferences() {
    Preferences preferences = Preferences.userNodeForPackage(getClass());
    PreferencesUtils.read(theme, preferences, Themes.getDefault()::forName);
    PreferencesUtils.read(defaultTileSize, preferences);
    PreferencesUtils.read(confirmTabClose, preferences);
    PreferencesUtils.read(saveFile, preferences, File::new);
    PreferencesUtils.read(autoLoadLastSaveFile, preferences);
    PreferencesUtils.read(confirmExit, preferences);

    theme.addListener(__ -> PreferencesUtils.save(theme, preferences, Theme::getName));
    defaultTileSize.addListener(__ -> PreferencesUtils.save(defaultTileSize, preferences));
    confirmTabClose.addListener(__ -> PreferencesUtils.save(confirmTabClose, preferences));
    saveFile.addListener(__ -> PreferencesUtils.save(saveFile, preferences, File::getAbsolutePath));
    autoLoadLastSaveFile.addListener(__ -> PreferencesUtils.save(autoLoadLastSaveFile, preferences));
    confirmExit.addListener(__ -> PreferencesUtils.save(confirmExit, preferences));
  }

  public static AppPreferences getInstance() {
    return instance;
  }

  public Category getSettings() {
    return settings;
  }

  public Property<Theme> themeProperty() {
    return theme;
  }

  public Theme getTheme() {
    return theme.getValue();
  }

  public void setTheme(Theme theme) {
    this.theme.setValue(theme);
  }

  public double getDefaultTileSize() {
    return defaultTileSize.get();
  }

  public DoubleProperty defaultTileSizeProperty() {
    return defaultTileSize;
  }

  public void setDefaultTileSize(double defaultTileSize) {
    this.defaultTileSize.set(defaultTileSize);
  }

  public boolean isConfirmTabClose() {
    return confirmTabClose.get();
  }

  public BooleanProperty confirmTabCloseProperty() {
    return confirmTabClose;
  }

  public void setConfirmTabClose(boolean confirmTabClose) {
    this.confirmTabClose.set(confirmTabClose);
  }

  public File getSaveFile() {
    return saveFile.getValue();
  }

  public Property<File> saveFileProperty() {
    return saveFile;
  }

  public void setSaveFile(File saveFile) {
    this.saveFile.setValue(saveFile);
  }

  public boolean isAutoLoadLastSaveFile() {
    return autoLoadLastSaveFile.get();
  }

  public BooleanProperty autoLoadLastSaveFileProperty() {
    return autoLoadLastSaveFile;
  }

  public void setAutoLoadLastSaveFile(boolean autoLoadLastSaveFile) {
    this.autoLoadLastSaveFile.set(autoLoadLastSaveFile);
  }

  public boolean isConfirmExit() {
    return confirmExit.get();
  }

  public BooleanProperty confirmExitProperty() {
    return confirmExit;
  }

  public void setConfirmExit(boolean confirmExit) {
    this.confirmExit.set(confirmExit);
  }
}
