package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.elements.NetworkTableTree;
import edu.wpi.first.shuffleboard.elements.TilePane;
import edu.wpi.first.shuffleboard.sources.CompositeNetworkTableSource;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.SingleKeyNetworkTableSource;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.shuffleboard.widget.Size;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static edu.wpi.first.shuffleboard.util.NetworkTableUtils.normalizeKey;

/**
 * Controller for the main UI window.
 */
public class MainWindowController {

  private static final Logger log = Logger.getLogger(MainWindowController.class.getName());

  @FXML
  private BorderPane root;
  @FXML
  private TilePane tileGrid;
  @FXML
  private NetworkTableTree networkTables;

  private final ITable rootTable = NetworkTable.getTable("");

  // Keep track of the widgets and corresponding UI elements
  private final List<WidgetHandle> widgetHandles = new ArrayList<>();

  private static final PseudoClass selectedPseudoClass = PseudoClass.getPseudoClass("selected");

  private static final class WidgetHandle {
    private final Widget widget;
    private Size currentSize;
    private String sourceName;
    private Node uiElement;

    public WidgetHandle(Widget widget) {
      this.widget = widget;
      sourceName = widget.getSource().getName();
    }

    public Widget getWidget() {
      return widget;
    }

    public Size getCurrentSize() {
      return currentSize;
    }

    public void setCurrentSize(Size currentSize) {
      this.currentSize = currentSize;
    }

    public String getSourceName() {
      return sourceName;
    }

    public void setSourceName(String sourceName) {
      this.sourceName = sourceName;
    }

    public Node getUiElement() {
      return uiElement;
    }

    public void setUiElement(Node uiElement) {
      this.uiElement = uiElement;
    }
  }


  @FXML
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void initialize() throws IOException {
    // NetworkTable view init
    networkTables.getKeyColumn().setPrefWidth(199);
    networkTables.getValueColumn().setPrefWidth(199);

    networkTables.getSelectionModel()
                 .selectedItemProperty()
                 .addListener((obs, prev, cur) -> {
                   // Highlight the tiles for the currently selected data item
                   if (prev != null) {
                     highlight(prev, false);
                   }
                   if (cur != null) {
                     highlight(cur, true);
                   }
                 });

    root.setOnKeyTyped(event -> {
      // Press ESC to clear selection
      if (event.getCharacter().charAt(0) == 27) {
        networkTables.getSelectionModel().select(null);
        widgetHandles.stream()
                     .map(WidgetHandle::getUiElement)
                     .forEach(n -> n.pseudoClassStateChanged(selectedPseudoClass, false));
      }
    });

    networkTables.setOnContextMenuRequested(e -> {
      TreeItem<NetworkTableEntry> selectedItem =
          networkTables.getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
        return;
      }

      String key = normalizeKey(selectedItem.getValue().getKey()).substring(1);

      List<String> widgetNames = widgetNamesFor(key);
      if (widgetNames.isEmpty()) {
        // No known widgets that can show this data
        return;
      }

      ContextMenu menu = new ContextMenu();
      for (String widgetName : widgetNames) {
        MenuItem mi = new MenuItem("Show as: " + widgetName);
        mi.setOnAction(a -> {
          DataSource<?> source;
          if (rootTable.containsSubTable(key)) {
            // It's a composite data type like a motor controller
            ITable table = rootTable.getSubTable(key);
            source = new CompositeNetworkTableSource(
                table, DataType.valueOf(table.getString("~METADATA~/Type", null)));
          } else {
            // It's a single key-value pair
            source = new SingleKeyNetworkTableSource<>(
                rootTable, key, rootTable.getValue(key).getClass());
          }
          Widgets.createWidget(widgetName, source)
                 .ifPresent(this::addWidget);
        });
        menu.getItems().add(mi);
      }
      menu.show(root.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
  }

  private void highlight(TreeItem<NetworkTableEntry> prev, boolean doHighlight) {
    findWidgets(prev.getValue().getKey())
        .forEach(handle -> handle.getUiElement()
                                 .pseudoClassStateChanged(selectedPseudoClass, doHighlight));
    if (!prev.isLeaf()) {
      // Highlight all child widgets
      widgetHandles.stream()
                   .filter(h -> h.getSourceName().startsWith(prev.getValue().getKey().substring(1)))
                   .forEach(h -> h.getUiElement()
                                  .pseudoClassStateChanged(selectedPseudoClass, doHighlight));
    }
  }

  private List<WidgetHandle> findWidgets(String fullTableKey) {
    String k = normalizeKey(fullTableKey).substring(1);
    return widgetHandles.stream()
                        .filter(h -> h.sourceName.equals(k))
                        .collect(Collectors.toList());
  }

  /**
   * Gets a list of the names of all widgets that can display the data associated with the given
   * network table key.
   *
   * @param fullTableKey the full network table key (ie "/a/b/c" instead of just "c")
   */
  public List<String> widgetNamesFor(String fullTableKey) {
    String key = normalizeKey(fullTableKey).substring(1);
    if (rootTable.containsKey(key)) {
      // Queried a key-value
      return Widgets.widgetNamesForType(
          DataType.valueOf(rootTable.getValue(key).getClass()));
    } else if (rootTable.containsSubTable(key)) {
      // Queried a subtable (composite type)
      ITable table = rootTable.getSubTable(key);
      if (table.containsSubTable("~METADATA~")) {
        // check for metadata that describes the type
        String type = table.getSubTable("~METADATA~").getString("Type", null);
        DataType dataType = DataType.valueOf(type);
        if (type == null) {
          log.warning("No type specified in metadata table");
          dataType = DataType.Composite;
        } else if (dataType == DataType.Unknown) {
          log.warning("Unknown data type '" + type + "'"); //NOPMD
          dataType = DataType.Composite;
        }
        return Widgets.widgetNamesForType(dataType);
      } else {
        log.warning("No metadata table for table " + key); //NOPMD
        return Collections.emptyList();
      }
    } else {
      // No possible widgets
      log.warning("No table element corresponding to key '" + key + "'"); //NOPMD
      return Collections.emptyList();
    }
  }

  @FXML
  public void close() {
    log.info("Exiting app");
    System.exit(0);
  }

  /**
   * Adds a widget to the tile view in the first available location.
   *
   * @param widget the widget to add
   */
  public void addWidget(Widget<?> widget) {
    Pane view = widget.getView();
    double w = Math.max(tileGrid.getTileSize(), view.getPrefWidth());
    double h = Math.max(tileGrid.getTileSize(), view.getPrefHeight());
    Size size = new Size((int) (w / tileGrid.getTileSize()), (int) (h / tileGrid.getTileSize()));
    addWidget(widget, size);
  }

  /**
   * Adds a widget to the tile view in the first available location. The tile will be the specified
   * size.
   *
   * @param widget the widget to add
   * @param size   the size of the tile used to display the widget
   */
  public void addWidget(Widget<?> widget, Size size) {
    WidgetHandle handle = new WidgetHandle(widget);
    handle.setCurrentSize(size);
    widgetHandles.add(handle);
    Pane control = widget.getView();
    Node uiElement = tileGrid.addTile(control, size);
    control.setOnContextMenuRequested(e -> {
      ContextMenu menu = new ContextMenu();
      MenuItem remove = new MenuItem("Remove");
      remove.setOnAction(a -> {
        tileGrid.getChildren().remove(uiElement);
        widgetHandles.remove(handle);
      });
      addChangeMenus(menu, widget, handle, uiElement);
      menu.getItems().add(new SeparatorMenuItem());
      menu.getItems().add(remove);
      menu.show(root.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
    handle.setUiElement(uiElement);
    handle.getUiElement().pseudoClassStateChanged(selectedPseudoClass, true);
  }

  private void addChangeMenus(ContextMenu menu,
                              Widget<?> widget,
                              WidgetHandle handle,
                              Node uiElement) {
    Menu changeView = new Menu("Show as...");
    Widgets.widgetNamesForType(DataType.valueOf(widget.getSource().getData().getClass()))
           .stream()
           .sorted()
           .forEach(name -> {
             MenuItem changeItem = new MenuItem(name);
             if (name.equals(widget.getName())) {
               changeItem.setGraphic(new Label("✓"));
             } else {
               // only need to change if it's to another type
               changeItem.setOnAction(a -> {
                 widgetHandles.remove(handle);
                 tileGrid.getChildren().remove(uiElement);
                 Widgets.createWidget(name, widget.getSource())
                        .ifPresent(this::addWidget);
               });
             }
             changeView.getItems().add(changeItem);
           });
    menu.getItems().add(changeView);
  }

}
