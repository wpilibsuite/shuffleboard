package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.components.WidgetGallery;
import edu.wpi.first.shuffleboard.dnd.DataFormats;
import edu.wpi.first.shuffleboard.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.prefs.ObservableItem;
import edu.wpi.first.shuffleboard.prefs.PropertyEditorFactory;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.sources.recording.Playback;
import edu.wpi.first.shuffleboard.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.theme.Theme;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.util.Storage;
import edu.wpi.first.shuffleboard.widget.NetworkTableTreeWidget;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;

import org.controlsfx.control.PropertySheet;
import org.fxmisc.easybind.EasyBind;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
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

import static edu.wpi.first.shuffleboard.util.TypeUtils.optionalCast;


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
  private Tab sourcesTab;

  private final ObservableValue<List<String>> stylesheets
      = EasyBind.map(AppPreferences.getInstance().themeProperty(), Theme::getStyleSheets);

  @FXML
  private void initialize() throws IOException {
    recordingMenu.textProperty().bind(
        EasyBind.map(
            Recorder.getInstance().runningProperty(),
            running -> running ? "Stop recording" : "Start recording"));
    FxUtils.bind(root.getStylesheets(), stylesheets);
    NetworkTableTreeWidget networkTables = new NetworkTableTreeWidget();
    networkTables.getTree().setRowFactory(view -> {
      TreeTableRow<NetworkTableEntry> row = new TreeTableRow<>();
      row.hoverProperty().addListener((__, wasHover, isHover) -> {
        if (!row.isEmpty()) {
          setHighlightedWithChildren(row.getTreeItem(), isHover);
        }
      });
      makeSourceRowDraggable(row);
      return row;
    });
    networkTables.getTree().setOnContextMenuRequested(e -> {
      TreeItem<NetworkTableEntry> selectedItem = networkTables.getTree().getSelectionModel().getSelectedItem();
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

      menu.show(root.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
    networkTables.setSource(NetworkTableSource.forKey(""));
    sourcesTab.setContent(networkTables.getView());

    widgetGallery.loadWidgets(Widgets.allWidgets());
  }

  private void makeSourceRowDraggable(TreeTableRow<? extends SourceEntry> row) {
    row.setOnDragDetected(event -> {
      if (row.isEmpty()) {
        return;
      }
      Dragboard dragboard = row.startDragAndDrop(TransferMode.COPY_OR_MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(DataFormats.source, row.getTreeItem().getValue());
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
   * Highlight or de-highlight any widgets with sources that are descendants of this NT key.
   */
  private void setHighlightedWithChildren(TreeItem<NetworkTableEntry> node,
                                          boolean highlightValue) {
    String key = node.getValue().getKey();

    if (highlightValue) {
      dashboard.selectWidgets((Widget widget) ->
          optionalCast(widget.getSource(), NetworkTableSource.class)
              .map(s ->
                  s.getKey().equals(key) || (!node.isLeaf() && s.getKey().startsWith(key))
              )
              .orElse(false)
      );
    } else {
      dashboard.selectWidgets(widget -> false);
    }
  }

  @FXML
  public void close() {
    log.info("Exiting app");
    System.exit(0);
  }

  /**
   * Shows the preferences window.
   */
  @SuppressWarnings("unchecked")
  @FXML
  public void showPrefs() {
    // Create the property sheet
    PropertySheet propertySheet = new PropertySheet();
    propertySheet.setModeSwitcherVisible(false);
    propertySheet.setSearchBoxVisible(false);
    propertySheet.setMode(PropertySheet.Mode.NAME);
    AppPreferences.getInstance().getProperties()
        .stream()
        .map(property -> new ObservableItem(property, "Application"))
        .forEachOrdered(propertySheet.getItems()::add);
    propertySheet.setPropertyEditorFactory(new PropertyEditorFactory());
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

}
