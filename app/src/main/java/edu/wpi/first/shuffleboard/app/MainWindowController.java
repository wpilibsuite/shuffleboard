package edu.wpi.first.shuffleboard.app;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

import edu.wpi.first.shuffleboard.api.components.SourceTreeTable;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.widget.Widgets;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.components.WidgetGallery;
import edu.wpi.first.shuffleboard.app.components.WidgetPropertySheet;
import edu.wpi.first.shuffleboard.app.json.JsonBuilder;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.app.sources.recording.Playback;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;
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

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.alphabetical;
import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.branchesFirst;
import static edu.wpi.first.shuffleboard.api.util.TypeUtils.optionalCast;


/**
 * Controller for the main UI window.
 */
public class MainWindowController {

  private static final Logger log = Logger.getLogger(MainWindowController.class.getName());

  @FXML
  private MenuItem recordingMenu;
  @FXML
  private WidgetGallery widgetGallery;
  @FXML
  private BorderPane root;
  @FXML
  private DashboardTabPane dashboard;
  @FXML
  private Accordion sourcesAccordion;
  @FXML
  private Pane pluginPane;
  private Stage pluginStage;

  private SourceEntry selectedEntry;

  File currentFile = null;

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

    setUpPluginsStage();
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
        List<String> widgetNames = Widgets.getDefault().widgetNamesForSource(source);
        if (widgetNames.isEmpty()) {
          // No known widgets that can show this data
          return;
        }

        ContextMenu menu = new ContextMenu();
        widgetNames.stream()
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
    widgetGallery.setWidgets(Widgets.getDefault().allWidgets());
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
        .filter(tab -> tab instanceof DashboardTabPane.DashboardTab)
        .map(tab -> (DashboardTabPane.DashboardTab) tab)
        .map(DashboardTabPane.DashboardTab::getWidgetPane)
        .forEach(pane -> {
          pane.getTiles().stream()
              .filter(tile -> plugin.getWidgets()
                  .contains(tile.getWidget().getClass()))
              .collect(Collectors.toList()) // collect into temporary list to prevent comodification
              .forEach(tile -> pane.getChildren().remove(tile));
        });
    // ... and from the gallery
    widgetGallery.setWidgets(Widgets.getDefault().allWidgets());
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

  private MenuItem createShowAsMenuItem(String widgetName, DataSource<?> source) {
    MenuItem menuItem = new MenuItem("Show as: " + widgetName);
    menuItem.setOnAction(action -> {
      Widgets.getDefault().createWidget(widgetName, source)
          .ifPresent(dashboard::addWidgetToActivePane);
    });
    return menuItem;
  }

  /**
   * Set the currently loaded dashboard.
   */
  public void setDashboard(DashboardTabPane dashboard) {
    dashboard.setId("dashboard");
    this.dashboard = dashboard;
    root.setCenter(dashboard);
  }

  @FXML
  public void close() {
    log.info("Exiting app");
    System.exit(0);
  }

  /**
   * Save the dashboard to an existing file, if one exists.
   * Otherwise is identical to #saveAs.
   */
  @FXML
  public void save() {
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
  private void saveAs() {
    FileChooser chooser = new FileChooser();
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("SmartDashboard Save File (.json)", "*.json"));
    if (currentFile == null) {
      chooser.setInitialDirectory(new File(Storage.STORAGE_DIR));
      chooser.setInitialFileName("smartdashboard.json");
    } else {
      chooser.setInitialDirectory(currentFile.getAbsoluteFile().getParentFile());
      chooser.setInitialFileName(currentFile.getName());
    }

    File selected = chooser.showSaveDialog(root.getScene().getWindow());

    saveFile(selected);
  }

  private void saveFile(File selected) {
    JsonBuilder.forSaveFile().toJson(dashboard, DashboardTabPane.class, System.out);
    try {
      Writer writer = Files.newWriter(selected, Charset.forName("UTF-8"));

      JsonBuilder.forSaveFile().toJson(dashboard, DashboardTabPane.class, writer);
      writer.flush();
    } catch (Exception e) {
      log.log(Level.WARNING, "Couldn't save", e);
      return;
    }

    currentFile = selected;
  }


  /**
   * Load the dashboard from a save file.
   */
  @FXML
  public void load() {
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(new File(Storage.STORAGE_DIR));
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("SmartDashboard Save File (.json)", "*.json"));

    final File selected = chooser.showOpenDialog(root.getScene().getWindow());

    if (selected == null) {
      return;
    }

    try {
      Reader reader = Files.newReader(selected, Charset.forName("UTF-8"));

      setDashboard(JsonBuilder.forSaveFile().fromJson(reader, DashboardTabPane.class));
    } catch (Exception e) {
      log.log(Level.WARNING, "Couldn't load", e);
      return;
    }

    currentFile = selected;
  }

  /**
   * Shows the preferences window.
   */
  @SuppressWarnings("unchecked")
  @FXML
  public void showPrefs() {
    PropertySheet propertySheet
        = new WidgetPropertySheet(AppPreferences.getInstance().getProperties());

    propertySheet.setModeSwitcherVisible(false);
    propertySheet.setSearchBoxVisible(false);
    propertySheet.setMode(PropertySheet.Mode.NAME);

    Dialog<Boolean> dialog = new Dialog<>();
    EasyBind.listBind(dialog.getDialogPane().getStylesheets(), root.getStylesheets());
    dialog.getDialogPane().setContent(propertySheet);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    dialog.setTitle("Shuffleboard Preferences");
    dialog.setResizable(true);
    dialog.setResultConverter(button -> !button.getButtonData().isCancelButton());
    if (dialog.showAndWait().orElse(false)) {
      propertySheet.getItems().stream()
          .map(item -> (WidgetPropertySheet.PropertyItem) item)
          .map(WidgetPropertySheet.PropertyItem::getObservableValue)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(o -> optionalCast(o, FlushableProperty.class))
          .filter(Optional::isPresent)
          .map(Optional::get)
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
    chooser.setInitialDirectory(new File(Storage.RECORDING_DIR));
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
  private void newTab() {
    DashboardTabPane.DashboardTab newTab = dashboard.addNewTab();
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
