package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.NetworkTableTree;
import edu.wpi.first.shuffleboard.components.WidgetPane;
import edu.wpi.first.shuffleboard.dnd.DataSourceTransferable;
import edu.wpi.first.shuffleboard.dnd.SourceRow;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.Widgets;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Controller for the main UI window.
 */
public class MainWindowController {

  private static final Logger log = Logger.getLogger(MainWindowController.class.getName());

  @FXML
  private BorderPane root;
  @FXML
  private WidgetPane widgetPane;
  @FXML
  private NetworkTableTree networkTables;

  private static final PseudoClass selectedPseudoClass = PseudoClass.getPseudoClass("selected");

  @FXML
  private void initialize() throws IOException {
    // NetworkTable view init
    networkTables.getKeyColumn().setPrefWidth(199);
    networkTables.getValueColumn().setPrefWidth(199);

    networkTables.setRowFactory(view -> {
      SourceRow<NetworkTableEntry> row = new SourceRow<>();
      row.hoverProperty().addListener((__, wasHover, isHover) -> {
        if (!row.isEmpty()) {
          highlight(row.getTreeItem(), isHover);
        }
      });
      row.setEntryConverter(entry -> DataSourceTransferable.networkTable(entry.getKey()));
      return row;
    });

    networkTables.setOnContextMenuRequested(e -> {
      TreeItem<NetworkTableEntry> selectedItem =
          networkTables.getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
        return;
      }

      String key = NetworkTableUtils.normalizeKey(selectedItem.getValue().getKey(), false);

      List<String> widgetNames = Widgets.widgetNamesForSource(NetworkTableSource.forKey(key));
      if (widgetNames.isEmpty()) {
        // No known widgets that can show this data
        return;
      }

      DataSource<?> source = NetworkTableSource.forKey(key);

      ContextMenu menu = new ContextMenu();
      widgetNames.stream()
                 .map(name -> createShowAsMenuItem(name, source))
                 .forEach(menu.getItems()::add);

      menu.show(root.getScene().getWindow(), e.getScreenX(), e.getScreenY());
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

  private void highlight(TreeItem<NetworkTableEntry> node, boolean doHighlight) {
    findWidgets(node.getValue().getKey())
        .forEach(handle -> highlight(handle.getUiElement(), doHighlight));

    if (!node.isLeaf()) {
      // Highlight all child widgets
      widgetPane.getWidgetHandles()
                .stream()
                .filter(handle -> handle.getSourceName()
                                        .startsWith(node.getValue().getKey().substring(1)))
                .forEach(handle -> highlight(handle.getUiElement(), doHighlight));
    }
  }

  private void highlight(Node tile, boolean doHighlight) {
    tile.pseudoClassStateChanged(selectedPseudoClass, doHighlight);
  }

  /**
   * Deselects all widgets in the tile view.
   */
  private void deselectAllWidgets() {
    widgetPane.getWidgetHandles()
              .stream()
              .map(WidgetHandle::getUiElement)
              .forEach(node -> highlight(node, false));
  }

  /**
   * Finds all widgets in the tile grid that are associated with the given network table key.
   */
  private List<WidgetHandle> findWidgets(String fullTableKey) {
    String key = NetworkTableUtils.normalizeKey(fullTableKey, false);
    return widgetPane.getWidgetHandles()
                     .stream()
                     .filter(handle -> handle.getSourceName().equals(key))
                     .collect(Collectors.toList());
  }

  @FXML
  public void close() {
    log.info("Exiting app");
    System.exit(0);
  }

}
