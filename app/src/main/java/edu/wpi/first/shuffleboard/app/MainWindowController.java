package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.app.components.AdderTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.dialogs.AboutDialog;
import edu.wpi.first.shuffleboard.app.dialogs.ExportRecordingDialog;
import edu.wpi.first.shuffleboard.app.dialogs.PluginDialog;
import edu.wpi.first.shuffleboard.app.dialogs.PrefsDialog;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.prefs.SettingsDialog;
import edu.wpi.first.shuffleboard.app.sources.recording.Playback;
import edu.wpi.first.shuffleboard.app.tab.TabInfoRegistry;

import org.fxmisc.easybind.EasyBind;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * Controller for the main UI window.
 */
@SuppressWarnings("PMD.TooManyFields")
public class MainWindowController {

  private static final Logger log = Logger.getLogger(MainWindowController.class.getName());

  @FXML
  private MenuItem recordingMenu;
  @FXML
  private Pane root;
  @FXML
  private Pane contentRoot;
  @FXML
  private Pane leftDrawer;
  @FXML
  private DashboardTabPane dashboard;

  private final PluginDialog pluginDialog = new PluginDialog();
  private final AboutDialog aboutDialog = new AboutDialog();
  private final ExportRecordingDialog exportRecordingDialog = new ExportRecordingDialog();
  private final PrefsDialog prefsDialog = new PrefsDialog();

  private final SaveFileHandler saveFileHandler = new SaveFileHandler();

  private final ObservableValue<List<String>> stylesheets
      = EasyBind.map(AppPreferences.getInstance().themeProperty(), Theme::getStyleSheets);

  @FXML
  private void initialize() {
    recordingMenu.textProperty().bind(
        EasyBind.map(
            Recorder.getInstance().runningProperty(),
            running -> running ? "Stop recording" : "Start recording"));
    FxUtils.bind(root.getStylesheets(), stylesheets);

    log.info("Setting up plugins in the UI");
    PluginLoader.getDefault().getLoadedPlugins().forEach(this::tearDownPluginWhenUnloaded);
    PluginLoader.getDefault().getKnownPlugins().addListener((ListChangeListener<Plugin>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(this::tearDownPluginWhenUnloaded);
        }
      }
    });

    root.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
      if (e.isShortcutDown() && e.getCode().isDigitKey()) {
        /*
         * Numpad digits have a name of "Numpad n"; where n is the number. We need to remove the
         * leading "Numpad " to parse the number.  Digit keys do not have this issue.
         */
        int digitPressed = Integer.parseInt(e.getCode().getName().replace("Numpad ", ""));
        dashboard.selectTab(digitPressed - 1);
      }
    });

    setLeftDrawerCallbacks();
  }

  private void tearDownPluginWhenUnloaded(Plugin plugin) {
    plugin.loadedProperty().addListener((__, was, is) -> {
      if (!is) {
        tearDown(plugin);
      }
    });
  }

  private void setLeftDrawerCallbacks() {
    LeftDrawerController leftDrawerController = FxUtils.getController(leftDrawer);
    leftDrawerController.setAddComponentToActivePane(dashboard::addComponentToActivePane);
    leftDrawerController.setCreateTabForSource(dashboard::createTabForSource);
    dashboard.getSelectionModel().selectedItemProperty().addListener((__, old, item) -> {
      if (!(item instanceof AdderTab)) {
        leftDrawerController.hide();
      }
    });
  }

  /**
   * Removes all traces from a plugin from the application window. Source trees will be removed and all widgets
   * defined by the plugin will be removed from all dashboard tabs.
   */
  private void tearDown(Plugin plugin) {
    // Remove widgets
    dashboard.getTabs().stream()
        .filter(tab -> tab instanceof DashboardTab)
        .map(tab -> (DashboardTab) tab)
        .map(DashboardTab::getWidgetPane)
        .forEach(pane ->
            pane.getTiles().stream()
                .filter(tile -> plugin.getComponents().stream()
                    .anyMatch(t -> tile.getContent().getName().equals(t.getName())))
                .collect(Collectors.toList()) // collect into temporary list to prevent comodification
                .forEach(tile -> pane.getChildren().remove(tile)));
  }

  /**
   * Set the currently loaded dashboard.
   */
  private void setDashboard(DashboardTabPane dashboard) {
    dashboard.setId("dashboard");
    contentRoot.getChildren().remove(this.dashboard);
    this.dashboard.getTabs().clear(); // Lets tabs get cleaned up (e.g. cancelling deferred autopopulation calls)
    this.dashboard = dashboard;
    setLeftDrawerCallbacks();
    dashboard.getSelectionModel().selectedItemProperty().addListener((__, old, item) -> {
      if (!(item instanceof AdderTab)) {
        FxUtils.<LeftDrawerController>getController(leftDrawer).hide();
      }
    });
    contentRoot.getChildren().add(0, dashboard);
  }

  /**
   * Sets the dashboard.
   */
  public void setDashboard(DashboardData dashboardData) {
    if (dashboardData == null) {
      return;
    }
    setDashboard(dashboardData.getTabPane());
    Platform.runLater(() -> {
      // Check that the window will be visible with the saved position and size
      WindowGeometry wg = dashboardData.getWindowGeometry();
      if (wg != null) {
        Window window = root.getScene().getWindow();
        List<Screen> screens = Screen.getScreensForRectangle(wg.getX(), wg.getY(), wg.getWidth(), wg.getHeight());
        if (!screens.isEmpty()) {
          window.setX(wg.getX());
          window.setY(wg.getY());
        }
        window.setWidth(wg.getWidth());
        window.setHeight(wg.getHeight());
      }
    });
  }

  /**
   * Closes from interacting with the "Close" menu item.
   */
  @FXML
  public void close() {
    log.info("Exiting app");

    // Attempt to close the main window. This lets window closing handlers run. Calling System.exit() or Platform.exit()
    // will more-or-less immediately terminate the application without calling these handlers.
    FxUtils.requestClose(root.getScene().getWindow());
    dashboard.getTabs().clear();
  }

  private DashboardData getData() {
    return new DashboardData(
        dashboard,
        new WindowGeometry(root.getScene().getWindow())
    );
  }

  /**
   * Save the dashboard to an existing file, if one exists.
   * Otherwise is identical to #saveAs.
   */
  @FXML
  public void save() throws IOException {
    saveFileHandler.save(getData());
  }

  /**
   * Choose a new file and save the dashboard to that file.
   */
  @FXML
  private void saveAs() throws IOException {
    saveFileHandler.saveAs(getData());
  }

  /**
   * Generates a new layout.
   */
  @FXML
  public void newLayout() {
    saveFileHandler.clear();
    List<TabInfo> tabInfo = TabInfoRegistry.getDefault().getItems();
    if (tabInfo.isEmpty()) {
      // No tab info, so add a placeholder tab so there's SOMETHING in the dashboard
      setDashboard(new DashboardTabPane(new DashboardTab("Tab 1")));
    } else {
      setDashboard(new DashboardTabPane(tabInfo));
    }
  }

  /**
   * Load the dashboard from a save file.
   */
  @FXML
  public void load() throws IOException {
    setDashboard(saveFileHandler.load());
  }

  /**
   * Loads the dashboard from a save file.
   *
   * @param saveFile the save file for the dashboard to load
   *
   * @throws IOException if the file could not be read from
   */
  public void load(File saveFile) throws IOException {
    setDashboard(saveFileHandler.load(saveFile));
  }

  @FXML
  public void showPrefs() {
    prefsDialog.show(dashboard);
  }

  @FXML
  private void toggleRecording() {
    if (Recorder.getInstance().isRunning()) {
      Recorder.getInstance().stop();
    } else {
      Recorder.getInstance().start();
    }
  }

  @FXML
  private void loadPlayback() throws IOException {
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(Storage.getRecordingDir());
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Shuffleboard Data Recording", "*.sbr"));
    final File selected = chooser.showOpenDialog(root.getScene().getWindow());
    if (selected == null) {
      return;
    }
    Playback playback = Playback.load(selected.getAbsolutePath());
    playback.start();
  }

  @FXML
  private void exportRecordings() {
    exportRecordingDialog.show();
  }

  @FXML
  private void closeCurrentTab() {
    dashboard.closeCurrentTab();
  }

  @FXML
  private void showTabPrefs() {
    List<Category> categories = dashboard.getTabs().stream()
        .flatMap(TypeUtils.castStream(DashboardTab.class))
        .map(DashboardTab::getSettings)
        .collect(Collectors.toList());
    SettingsDialog dialog = new SettingsDialog(categories);
    dialog.getDialogPane().getStylesheets().setAll(stylesheets.getValue());
    dialog.setTitle("Tab Preferences");
    dialog.showAndWait();
  }

  @FXML
  private void newTab() {
    DashboardTab newTab = dashboard.addNewTab();
    dashboard.getSelectionModel().select(newTab);
  }

  @FXML
  private void showPlugins() {
    pluginDialog.show();
  }

  @FXML
  private void showAboutDialog() {
    aboutDialog.show();
  }

}
