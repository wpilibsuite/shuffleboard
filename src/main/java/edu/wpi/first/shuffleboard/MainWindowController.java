package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.NetworkTableTree;
import edu.wpi.first.shuffleboard.components.WidgetGallery;
import edu.wpi.first.shuffleboard.components.WidgetPane;
import edu.wpi.first.shuffleboard.dnd.DataFormats;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.sources.recording.Playback;
import edu.wpi.first.shuffleboard.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.util.Storage;
import edu.wpi.first.shuffleboard.widget.Widgets;

import org.fxmisc.easybind.EasyBind;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

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
  private WidgetPane widgetPane;
  @FXML
  private NetworkTableTree networkTables;

  private static final PseudoClass selectedPseudoClass = PseudoClass.getPseudoClass("selected");

  @FXML
  private void initialize() throws IOException {
    recordingMenu.textProperty().bind(
        EasyBind.map(
            Recorder.getInstance().runningProperty(),
            running -> running ? "Stop recording" : "Start recording"));
    // NetworkTable view init
    networkTables.getKeyColumn().setPrefWidth(199);
    networkTables.getValueColumn().setPrefWidth(199);

    networkTables.setRowFactory(view -> {
      TreeTableRow<NetworkTableEntry> row = new TreeTableRow<>();
      row.hoverProperty().addListener((__, wasHover, isHover) -> {
        if (!row.isEmpty()) {
          setHighlightedWithChildren(row.getTreeItem(), isHover);
        }
      });
      makeSourceRowDraggable(row);
      return row;
    });

    networkTables.setOnContextMenuRequested(e -> {
      TreeItem<NetworkTableEntry> selectedItem =
          networkTables.getSelectionModel().getSelectedItem();
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
             .ifPresent(widgetPane::addWidget);
    });
    return menuItem;
  }

  /**
   * Highlight or de-highlight any widgets with sources that are descendants of this NT key.
   */
  private void setHighlightedWithChildren(TreeItem<NetworkTableEntry> node,
                                          boolean highlightValue) {
    String key = node.getValue().getKey();

    widgetPane.getTiles()
              .stream()
              .filter(tile ->
                optionalCast(tile.getWidget().getSource(), NetworkTableSource.class)
                      .map(s ->
                        s.getKey().equals(key) || (!node.isLeaf() && s.getKey().startsWith(key))
                      )
                      .orElse(false)
              )
              .forEach(tile -> setHighlighted(tile, highlightValue));
  }

  private void setHighlighted(Node tile, boolean highlightValue) {
    tile.pseudoClassStateChanged(selectedPseudoClass, highlightValue);
  }

  /**
   * Deselects all widgets in the tile view.
   */
  private void deselectAllWidgets() {
    widgetPane.getTiles().forEach(node -> setHighlighted(node, false));
  }

  @FXML
  public void close() {
    log.info("Exiting app");
    System.exit(0);
  }

  @FXML
  private void startRecording() {
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
