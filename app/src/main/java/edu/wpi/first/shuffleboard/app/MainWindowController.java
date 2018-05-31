package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.ConnectionStatus;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.UiHints;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.components.InteractiveSourceTree;
import edu.wpi.first.shuffleboard.app.components.WidgetGallery;
import edu.wpi.first.shuffleboard.app.dialogs.AboutDialog;
import edu.wpi.first.shuffleboard.app.dialogs.ExportRecordingDialog;
import edu.wpi.first.shuffleboard.app.dialogs.PluginDialog;
import edu.wpi.first.shuffleboard.app.dialogs.PrefsDialog;
import edu.wpi.first.shuffleboard.app.dialogs.RestartPromptDialog;
import edu.wpi.first.shuffleboard.app.dialogs.UpdateDownloadDialog;
import edu.wpi.first.shuffleboard.app.json.JsonBuilder;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.sources.recording.Playback;
import edu.wpi.first.shuffleboard.app.tab.TabInfoRegistry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

import org.fxmisc.easybind.EasyBind;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import static edu.wpi.first.shuffleboard.api.util.ListUtils.joining;


/**
 * Controller for the main UI window.
 */
@SuppressWarnings("PMD.GodClass") // TODO refactor this class
public class MainWindowController {

  private static final Logger log = Logger.getLogger(MainWindowController.class.getName());

  @FXML
  private MenuItem recordingMenu;
  @FXML
  private WidgetGallery widgetGallery;
  @FXML
  private Pane root;
  @FXML
  private SplitPane centerSplitPane;
  @FXML
  private DashboardTabPane dashboard;
  @FXML
  private Accordion sourcesAccordion;
  @FXML
  private Pane updateFooter;
  @FXML
  private HBox connectionIndicatorArea;

  private final PluginDialog pluginDialog = new PluginDialog();
  private final AboutDialog aboutDialog = new AboutDialog();
  private final ExportRecordingDialog exportRecordingDialog = new ExportRecordingDialog();
  private final UpdateDownloadDialog updateDownloadDialog = new UpdateDownloadDialog();
  private final RestartPromptDialog restartPromptDialog = new RestartPromptDialog();
  private final PrefsDialog prefsDialog = new PrefsDialog();

  private File currentFile = null;

  private final ObservableValue<List<String>> stylesheets
      = EasyBind.map(AppPreferences.getInstance().themeProperty(), Theme::getStyleSheets);

  private final Multimap<Plugin, TitledPane> sourcePanes = ArrayListMultimap.create();
  private final ShuffleboardUpdateChecker shuffleboardUpdateChecker = new ShuffleboardUpdateChecker();
  private final ExecutorService updateCheckingExecutor
      = Executors.newSingleThreadExecutor(ThreadUtils::makeDaemonThread);

  @FXML
  private void initialize() throws IOException {
    recordingMenu.textProperty().bind(
        EasyBind.map(
            Recorder.getInstance().runningProperty(),
            running -> running ? "Stop recording" : "Start recording"));
    FxUtils.bind(root.getStylesheets(), stylesheets);

    log.info("Setting up plugins in the UI");
    PluginLoader.getDefault().getLoadedPlugins().forEach(plugin -> {
      plugin.loadedProperty().addListener((__, was, is) -> {
        if (is) {
          setup(plugin);
        } else {
          tearDown(plugin);
        }
      });
      setup(plugin);
    });
    sourcesAccordion.getPanes().sort(Comparator.comparing(TitledPane::getText));
    PluginLoader.getDefault().getKnownPlugins().addListener((ListChangeListener<Plugin>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(plugin -> {
            plugin.loadedProperty().addListener((__, was, is) -> {
              if (is) {
                setup(plugin);
              } else {
                tearDown(plugin);
              }
            });
            setup(plugin);
          });
        }
        sourcesAccordion.getPanes().sort(Comparator.comparing(TitledPane::getText));
      }
    });

    root.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
      if (e.isShortcutDown() && e.getCode().isDigitKey()) {
        /*
         * Numpad digits have a name of "Numpad n"; where n is the number. We need to remove the
         * leading "Numpad " to parse the number.  Digit keys do not have this issue.
         */
        int digitPressed = Integer.valueOf(e.getCode().getName().replace("Numpad ", ""));
        dashboard.selectTab(digitPressed - 1);
      }
    });

    generateConnectionIndicators(SourceTypes.getDefault().getItems());
    SourceTypes.getDefault().getItems().addListener((InvalidationListener) items -> {
      generateConnectionIndicators((ObservableList<SourceType>) items);
    });
  }

  private void generateConnectionIndicators(List<SourceType> sourceTypes) {
    connectionIndicatorArea.getChildren().setAll(
        sourceTypes.stream()
            .filter(s -> !optOutOfConnectionIndicator(s))
            .map(this::generateConnectionLabel)
            .collect(joining(this::generateSeparatorLabel)));
  }

  private Label generateSeparatorLabel() {
    Label label = new Label(" | ");
    label.getStyleClass().add("connection-indicator-separator");
    return label;
  }

  /**
   * Checks if a source type has opted out of displaying a connection indicator.
   */
  private boolean optOutOfConnectionIndicator(SourceType sourceType) {
    Class<? extends SourceType> clazz = sourceType.getClass();
    UiHints hints = clazz.getAnnotation(UiHints.class);
    return hints != null && !hints.showConnectionIndicator();
  }

  private Label generateConnectionLabel(SourceType sourceType) {
    Label label = new Label();
    label.getStyleClass().add("connection-indicator");
    label.textProperty().bind(
        EasyBind.monadic(sourceType.connectionStatusProperty())
            .map(ConnectionStatus::isConnected)
            .map(connected -> sourceType.getName() + ": " + (connected ? "connected" : "not connected")));
    sourceType.connectionStatusProperty().addListener((__, old, status) -> {
      updateConnectionLabel(label, status.isConnected());
    });
    updateConnectionLabel(label, sourceType.getConnectionStatus().isConnected());
    return label;
  }

  private void updateConnectionLabel(Label label, boolean connected) {
    label.pseudoClassStateChanged(PseudoClass.getPseudoClass("connected"), connected);
    label.pseudoClassStateChanged(PseudoClass.getPseudoClass("disconnected"), !connected);
  }

  /**
   * Sets up UI components to represent the sources that a plugin defines.
   */
  private void setup(Plugin plugin) {
    FxUtils.runOnFxThread(() -> {
      plugin.getSourceTypes().forEach(sourceType -> {
        InteractiveSourceTree tree =
            new InteractiveSourceTree(sourceType, dashboard::addComponentToActivePane, dashboard::createTabForSource);

        TitledPane titledPane = new TitledPane(sourceType.getName(), tree);
        sourcePanes.put(plugin, titledPane);
        sourcesAccordion.getPanes().add(titledPane);
        FXCollections.sort(sourcesAccordion.getPanes(), Comparator.comparing(TitledPane::getText));
        if (sourcesAccordion.getExpandedPane() == null) {
          sourcesAccordion.setExpandedPane(titledPane);
        }
      });

      // Add widgets to the gallery as well
      widgetGallery.setWidgets(Components.getDefault().allWidgets().collect(Collectors.toList()));
    });
  }

  /**
   * Removes all traces from a plugin from the application window. Source trees will be removed and all widgets
   * defined by the plugin will be removed from all dashboard tabs.
   */
  private void tearDown(Plugin plugin) {
    // Remove the source panes
    sourcesAccordion.getPanes().removeAll(sourcePanes.removeAll(plugin));
    FXCollections.sort(sourcesAccordion.getPanes(), Comparator.comparing(TitledPane::getText));
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
    // ... and from the gallery
    widgetGallery.setWidgets(Components.getDefault().allWidgets().collect(Collectors.toList()));
  }

  /**
   * Set the currently loaded dashboard.
   */
  public void setDashboard(DashboardTabPane dashboard) {
    dashboard.setId("dashboard");
    centerSplitPane.getItems().remove(this.dashboard);
    this.dashboard.getTabs().clear(); // Lets tabs get cleaned up (e.g. cancelling deferred autopopulation calls)
    this.dashboard = dashboard;
    centerSplitPane.getItems().add(dashboard);
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
  }

  /**
   * Save the dashboard to an existing file, if one exists.
   * Otherwise is identical to #saveAs.
   */
  @FXML
  public void save() throws IOException {
    if (currentFile == null) {
      saveAs();
    } else {
      saveFile(currentFile);
    }
  }

  /**
   * Choose a new file and save the dashboard to that file.
   */
  @FXML
  private void saveAs() throws IOException {
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

    Optional.ofNullable(chooser.showSaveDialog(root.getScene().getWindow()))
        .ifPresent(this::saveFile);
  }

  private void saveFile(File selected) {
    try {
      Writer writer = Files.newWriter(selected, Charset.forName("UTF-8"));

      DashboardData dashboardData = new DashboardData(centerSplitPane.getDividerPositions()[0], dashboard);
      JsonBuilder.forSaveFile().toJson(dashboardData, writer);
      writer.flush();
    } catch (Exception e) {
      log.log(Level.WARNING, "Couldn't save", e);
      return;
    }

    currentFile = selected;
    AppPreferences.getInstance().setSaveFile(currentFile);
  }

  /**
   * Generates a new layout.
   */
  @FXML
  public void newLayout() {
    currentFile = null;
    double[] dividerPositions = centerSplitPane.getDividerPositions();
    List<TabInfo> tabInfo = TabInfoRegistry.getDefault().getItems();
    if (tabInfo.isEmpty()) {
      // No tab info, so add a placeholder tab so there's SOMETHING in the dashboard
      setDashboard(new DashboardTabPane(new DashboardTab("Tab 1")));
    } else {
      setDashboard(new DashboardTabPane(tabInfo));
    }
    centerSplitPane.setDividerPositions(dividerPositions);
  }

  /**
   * Load the dashboard from a save file.
   */
  @FXML
  public void load() throws IOException {
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(Storage.getStorageDir());
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Shuffleboard Save File (.json)", "*.json"));

    final File selected = chooser.showOpenDialog(root.getScene().getWindow());
    load(selected);
  }

  /**
   * Loads a saved dashboard layout.
   *
   * @param saveFile the save file to load
   */
  public void load(File saveFile) throws IOException {
    if (saveFile == null) {
      return;
    }
    Reader reader = Files.newReader(saveFile, Charset.forName("UTF-8"));

    DashboardData dashboardData = JsonBuilder.forSaveFile().fromJson(reader, DashboardData.class);
    if (dashboardData == null) {
      throw new IOException("Save file could not be read: " + saveFile);
    }
    setDashboard(dashboardData.getTabPane());
    Platform.runLater(() -> {
      centerSplitPane.setDividerPositions(dashboardData.getDividerPosition());
    });

    currentFile = saveFile;
    AppPreferences.getInstance().setSaveFile(currentFile);
  }

  @FXML
  public void showPrefs() {
    prefsDialog.show();
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
  private void showCurrentTabPrefs() {
    Tab currentTab = dashboard.getSelectionModel().getSelectedItem();
    if (currentTab instanceof DashboardTab) {
      ((DashboardTab) currentTab).showPrefsDialog();
    }
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

  /**
   * Checks for updates to shuffleboard and prompts the user to update, if an update is available. The prompt
   * is displayed as a small footer bar across the bottom of the scene. If the check fails, no notifications are shown.
   * This differs from {@link #checkForUpdates()}, which will display all status updates as dialogs.
   */
  public void checkForUpdatesSubdued() {
    UpdateFooterController controller = FxUtils.getController(updateFooter);
    controller.setShuffleboardUpdateChecker(shuffleboardUpdateChecker);
    controller.checkForUpdatesAndPrompt();
  }

  /**
   * Checks for updates to shuffleboard, then prompts the user to update (if applicable) and restart to apply the
   * update. This also shows the download progress in a separate dialog, which can be closed or hidden.
   */
  @FXML
  public void checkForUpdates() {
    AtomicBoolean firstShow = new AtomicBoolean(true);
    final DoubleConsumer progressNotifier = value -> {
      FxUtils.runOnFxThread(() -> {
        // Show the dialog on the first update
        // Close the dialog when the download completes
        // If the user closes the dialog before then, don't re-open it
        if (value == 1) {
          updateDownloadDialog.close();
        } else if (!updateDownloadDialog.isShowing() && firstShow.get()) {
          updateDownloadDialog.show();
          firstShow.set(false);
        }
        updateDownloadDialog.setDownloadProgress(value);
      });
    };
    updateCheckingExecutor.submit(() ->
        shuffleboardUpdateChecker.checkForUpdatesAndPromptToInstall(progressNotifier, this::handleUpdateResult));
  }

  private void handleUpdateResult(ShuffleboardUpdateChecker.Result<Path> result) {
    // Make sure this runs on the JavaFX thread -- this method is not guaranteed to be called from it
    FxUtils.runOnFxThread(() -> {
      if (result.succeeded()) {
        restartPromptDialog.show(root.getScene().getWindow());
      } else {
        showFailureAlert(result);
      }
    });
  }

  private void showFailureAlert(ShuffleboardUpdateChecker.Result<Path> result) {
    Alert failureAlert = new Alert(Alert.AlertType.ERROR);
    FxUtils.bind(failureAlert.getDialogPane().getStylesheets(), stylesheets);
    failureAlert.setTitle("Update failed");
    failureAlert.setContentText("Error: " + result.getError().getMessage() + "\nSee the log for detailed information");
    failureAlert.showAndWait();
  }

}
