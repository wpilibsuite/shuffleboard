package edu.wpi.first.shuffleboard.app;

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
import edu.wpi.first.shuffleboard.app.sources.recording.Playback;

import org.controlsfx.control.PropertySheet;
import org.fxmisc.easybind.EasyBind;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.alphabetical;
import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.branchesFirst;


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

  private SourceEntry selectedEntry;

  File currentFile = null;

  private final ObservableValue<List<String>> stylesheets
      = EasyBind.map(AppPreferences.getInstance().themeProperty(), Theme::getStyleSheets);

  @FXML
  private void initialize() throws IOException {
    recordingMenu.textProperty().bind(
        EasyBind.map(
            Recorder.getInstance().runningProperty(),
            running -> running ? "Stop recording" : "Start recording"));
    FxUtils.bind(root.getStylesheets(), stylesheets);

    PluginLoader.getDefault().getLoadedPlugins().addListener((ListChangeListener<Plugin>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(plugin -> {
            plugin.getSourceTypes().forEach(sourceType -> {
              SourceTreeTable<SourceEntry<?>, ?> tree = new SourceTreeTable<>();
              tree.setSourceType(sourceType);
              tree.setRoot(new TreeItem<SourceEntry<?>>(sourceType.createRootSourceEntry()));
              tree.setShowRoot(false);
              tree.setSortPolicy(__ -> {
                sortTree(tree.getRoot());
                return true;
              });
              tree.getSelectionModel().selectedItemProperty().addListener((__, oldItem, newItem) -> {
                selectedEntry = newItem == null ? null : newItem.getValue();
              });
              tree.setRowFactory(__ -> {
                TreeTableRow<SourceEntry<?>> row = new TreeTableRow<>();
                makeSourceRowDraggable(row);
                return row;
              });
              tree.setOnContextMenuRequested(e -> {
                TreeItem<SourceEntry<?>> selectedItem = tree.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                  return;
                }

                DataSource<?> source = selectedItem.getValue().get();
                List<String> widgetNames = Widgets.widgetNamesForSource(source);
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
              sourcesAccordion.getPanes().add(titledPane);
              sourcesAccordion.setExpandedPane(titledPane);
            });
          });
        } else if (c.wasRemoved()) { //NOPMD empty if statement
          //TODO
        }
        sourcesAccordion.getPanes().sort(Comparator.comparing(TitledPane::getText));
      }
      widgetGallery.setWidgets(Widgets.allWidgets());
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
      Widgets.createWidget(widgetName, source)
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
    // Create the property sheet
    PropertySheet propertySheet = new WidgetPropertySheet(AppPreferences.getInstance().getProperties());
    propertySheet.setModeSwitcherVisible(false);
    propertySheet.setSearchBoxVisible(false);
    propertySheet.setMode(PropertySheet.Mode.NAME);
    StackPane pane = new StackPane(propertySheet);
    pane.setPadding(new Insets(8));
    Scene scene = new Scene(pane);
    EasyBind.listBind(scene.getRoot().getStylesheets(), root.getStylesheets());

    Stage stage = new Stage();
    stage.setScene(scene);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.setTitle("Shuffleboard Preferences");
    stage.sizeToScene();
    stage.setResizable(false);
    stage.requestFocus();
    stage.showAndWait();
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
    chooser.setInitialDirectory(new File(Storage.STORAGE_DIR));
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("FRC Data Recording", "*.frc"));
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

}
