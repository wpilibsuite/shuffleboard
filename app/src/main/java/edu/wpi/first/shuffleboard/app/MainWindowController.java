package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;
import edu.wpi.first.shuffleboard.api.components.SourceTreeTable;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.sources.ConnectionStatus;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.UiHints;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.components.WidgetGallery;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.alphabetical;
import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.branchesFirst;
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
  private HBox connectionIndicatorArea;
  @FXML
  private Pane pluginPane;
  private Stage pluginStage;

  private SourceEntry selectedEntry;

  private File currentFile = null;

  private final ObservableValue<List<String>> stylesheets
      = EasyBind.map(AppPreferences.getInstance().themeProperty(), Theme::getStyleSheets);

  private final Multimap<Plugin, TitledPane> sourcePanes = ArrayListMultimap.create();

  @FXML
  private void initialize() throws IOException {
    recordingMenu.textProperty().bind(
        EasyBind.map(
            Recorder.getInstance().runningProperty(),
            running -> running ? "Stop recording" : "Start recording"));
    FxUtils.bind(root.getStylesheets(), stylesheets);

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

    SourceTypes.getDefault().getItems().addListener((InvalidationListener) items -> {
      List<? extends Node> collect = ((ObservableList<SourceType>) items).stream()
          .filter(s -> !optOutOfConnectionIndicator(s))
          .map(this::generateConnectionLabel)
          .collect(joining(this::generateSeparatorLabel));
      connectionIndicatorArea.getChildren().setAll(collect);
    });

    setUpPluginsStage();
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

  private void setUpPluginsStage() {
    pluginStage = new Stage();
    pluginStage.initModality(Modality.WINDOW_MODAL);
    pluginStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        pluginStage.close();
      }
    });
    pluginStage.setScene(new Scene(pluginPane));
    pluginStage.sizeToScene();
    pluginStage.setMinWidth(675);
    pluginStage.setMinHeight(325);
    pluginStage.setTitle("Loaded Plugins");
    EasyBind.listBind(pluginPane.getStylesheets(), root.getStylesheets());
  }

  /**
   * Sets up UI components to represent the sources that a plugin defines.
   */
  private void setup(Plugin plugin) {
    plugin.getSourceTypes().forEach(sourceType -> {
      SourceTreeTable<SourceEntry, ?> tree = new SourceTreeTable<>();
      tree.setSourceType(sourceType);
      tree.setRoot(new TreeItem<>(sourceType.createRootSourceEntry()));
      tree.setShowRoot(false);
      tree.setSortPolicy(__ -> {
        sortTree(tree.getRoot());
        return true;
      });
      tree.getSelectionModel().selectedItemProperty().addListener((__, oldItem, newItem) -> {
        selectedEntry = newItem == null ? null : newItem.getValue();
      });
      tree.setRowFactory(__ -> {
        TreeTableRow<SourceEntry> row = new TreeTableRow<>();
        makeSourceRowDraggable(row);
        return row;
      });
      tree.setOnContextMenuRequested(e -> {
        TreeItem<SourceEntry> selectedItem = tree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
          return;
        }

        DataSource<?> source = selectedItem.getValue().get();
        List<String> componentNames = Components.getDefault().componentNamesForSource(source);
        if (componentNames.isEmpty()) {
          // No known components that can show this data
          return;
        }

        ContextMenu menu = new ContextMenu();
        componentNames.stream()
            .map(name -> createShowAsMenuItem(name, source))
            .forEach(menu.getItems()::add);

        menu.show(tree.getScene().getWindow(), e.getScreenX(), e.getScreenY());
      });
      sourceType.getAvailableSources().addListener((MapChangeListener<String, Object>) change -> {
        SourceEntry entry = sourceType.createSourceEntryForUri(change.getKey());
        if (change.wasAdded()) {
          tree.updateEntry(entry);
        } else if (change.wasRemoved()) {
          tree.removeEntry(entry);
        }
      });
      sourceType.getAvailableSourceUris().stream()
          .map(sourceType::createSourceEntryForUri)
          .forEach(tree::updateEntry);
      TitledPane titledPane = new TitledPane(sourceType.getName(), tree);
      sourcePanes.put(plugin, titledPane);
      sourcesAccordion.getPanes().add(titledPane);
      sourcesAccordion.setExpandedPane(titledPane);
    });

    // Add widgets to the gallery as well
    widgetGallery.setWidgets(Components.getDefault().allWidgets().collect(Collectors.toList()));
  }

  /**
   * Removes all traces from a plugin from the application window. Source trees will be removed and all widgets
   * defined by the plugin will be removed from all dashboard tabs.
   */
  private void tearDown(Plugin plugin) {
    // Remove the source panes
    sourcesAccordion.getPanes().removeAll(sourcePanes.removeAll(plugin));
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
   * Sorts tree nodes recursively in order of branches before leaves, then alphabetically.
   *
   * @param root the root node to sort
   */
  private void sortTree(TreeItem<? extends SourceEntry> root) {
    if (!root.isLeaf()) {
      FXCollections.sort(root.getChildren(),
          branchesFirst.thenComparing(alphabetical));
      root.getChildren().forEach(this::sortTree);
    }
  }

  private void makeSourceRowDraggable(TreeTableRow<? extends SourceEntry> row) {
    row.setOnDragDetected(event -> {
      if (selectedEntry == null) {
        return;
      }
      Dragboard dragboard = row.startDragAndDrop(TransferMode.COPY_OR_MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(DataFormats.source, selectedEntry);
      dragboard.setContent(content);
      event.consume();
    });
  }

  private MenuItem createShowAsMenuItem(String componentName, DataSource<?> source) {
    MenuItem menuItem = new MenuItem("Show as: " + componentName);
    menuItem.setOnAction(action -> {
      Components.getDefault().createComponent(componentName, source)
          .ifPresent(dashboard::addComponentToActivePane);
    });
    return menuItem;
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
    Window window = root.getScene().getWindow();
    window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
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
  public void load(File saveFile) {
    if (saveFile == null) {
      return;
    }
    try {
      Reader reader = Files.newReader(saveFile, Charset.forName("UTF-8"));

      DashboardData dashboardData = JsonBuilder.forSaveFile().fromJson(reader, DashboardData.class);
      setDashboard(dashboardData.getTabPane());
      Platform.runLater(() -> {
        centerSplitPane.setDividerPositions(dashboardData.getDividerPosition());
      });
    } catch (Exception e) {
      log.log(Level.WARNING, "Couldn't load", e);
      return;
    }

    currentFile = saveFile;
    AppPreferences.getInstance().setSaveFile(currentFile);
  }

  /**
   * Shows the preferences window.
   */
  @SuppressWarnings("unchecked")
  @FXML
  public void showPrefs() {
    TabPane tabs = new TabPane();
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    tabs.getTabs().add(new Tab("Application", new ExtendedPropertySheet(AppPreferences.getInstance().getProperties())));

    for (Plugin plugin : PluginLoader.getDefault().getLoadedPlugins()) {
      if (plugin.getProperties().isEmpty()) {
        continue;
      }
      Tab tab = new Tab(plugin.getName());
      tab.setContent(new ExtendedPropertySheet(plugin.getProperties()));

      tab.setDisable(DashboardMode.getCurrentMode() == DashboardMode.PLAYBACK);
      tabs.getTabs().add(tab);
    }

    Dialog<Boolean> dialog = new Dialog<>();
    EasyBind.listBind(dialog.getDialogPane().getStylesheets(), root.getStylesheets());
    dialog.getDialogPane().setContent(new BorderPane(tabs));
    dialog.initOwner(root.getScene().getWindow());
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.UTILITY);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    dialog.setTitle("Shuffleboard Preferences");
    dialog.setResizable(true);
    dialog.setResultConverter(button -> !button.getButtonData().isCancelButton());
    if (dialog.showAndWait().orElse(false)) {
      tabs.getTabs().stream()
          .map(t -> (ExtendedPropertySheet) t.getContent())
          .flatMap(p -> p.getItems().stream())
          .flatMap(TypeUtils.castStream(ExtendedPropertySheet.PropertyItem.class))
          .map(i -> (Optional<ObservableValue>) i.getObservableValue())
          .flatMap(TypeUtils.optionalStream())
          .flatMap(TypeUtils.castStream(FlushableProperty.class))
          .filter(FlushableProperty::isChanged)
          .forEach(FlushableProperty::flush);
    }
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
    if (pluginStage.getOwner() == null) {
      pluginStage.initOwner(root.getScene().getWindow());
    }
    pluginStage.show();
  }

}
