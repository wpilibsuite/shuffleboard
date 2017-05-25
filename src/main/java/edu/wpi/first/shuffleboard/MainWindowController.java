package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.sources.CompositeNetworkTableSource;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.SingleKeyNetworkTableSource;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.shuffleboard.widget.Size;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller for the main UI window.
 */
@SuppressWarnings("PMD.GodClass")
public class MainWindowController {

  private static final Logger log = Logger.getLogger(MainWindowController.class.getName());

  @FXML
  private BorderPane root;
  @FXML
  private GridPane tileGrid;
  @FXML
  private TreeTableView<NetworkTableEntry> networkTables;
  @FXML
  private TreeTableColumn<NetworkTableEntry, String> keyColumn;
  @FXML
  private TreeTableColumn<NetworkTableEntry, String> valueColumn;
  @FXML
  private TreeItem<NetworkTableEntry> networktableRoot;

  private final ITable rootTable = NetworkTable.getTable("");

  private int numCols;
  private int numRows;
  private static final int colWidth = 128;
  private static final int rowHeight = colWidth; // square tiles

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
    // Show network table data in the sidebar
    NetworkTablesJNI.addEntryListener(
        "",
        (uid, key, value, flags) -> makeBranches(key, value, flags),
        0xFF);

    // init grid TODO this should be in its own class
    numCols = 8;
    numRows = 6;
    for (int i = 0; i < numCols; i++) {
      tileGrid.getColumnConstraints().add(new ColumnConstraints(
          colWidth, colWidth, Double.POSITIVE_INFINITY, Priority.ALWAYS, HPos.CENTER, true));
    }
    for (int i = 0; i < numRows; i++) {
      tileGrid.getRowConstraints().add(new RowConstraints(
          rowHeight, rowHeight, Double.POSITIVE_INFINITY, Priority.ALWAYS, VPos.CENTER, true));
    }
    tileGrid.setGridLinesVisible(false);

    // NetworkTable view init
    keyColumn.setCellValueFactory(f -> new ReadOnlyStringWrapper(simpleKey(getEntry(f).getKey())));
    valueColumn.setCellValueFactory(f -> new ReadOnlyStringWrapper(getEntry(f).getValue()));

    networkTables.setSortPolicy(t -> {
      sort(networktableRoot);
      return true;
    });

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

  private NetworkTableEntry getEntry(CellDataFeatures<NetworkTableEntry, String> features) {
    return features.getValue().getValue();
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
          DataType.valueOf(NetworkTable.getTable("").getValue(key).getClass()));
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

  /**
   * Sorts tree nodes recursively in order of branches before leaves, then alphabetically.
   *
   * @param node the root node to sort
   */
  @SuppressWarnings("Indentation")
  private void sort(TreeItem<NetworkTableEntry> node) {
    if (!node.isLeaf()) {
      FXCollections.sort(node.getChildren(),
                         ((Comparator<TreeItem<NetworkTableEntry>>)
                             (a, b) -> a.isLeaf() ? b.isLeaf() ? 0 : 1 : -1)
                             .thenComparing(Comparator.comparing(item -> item.getValue().getKey()))
                        );
      node.getChildren().forEach(this::sort);
    }
  }

  private String simpleKey(String key) {
    if (!key.contains("/")) {
      return key;
    }
    return key.substring(key.lastIndexOf('/') + 1);
  }

  /**
   * Normalizes a network table key to start with exactly one leading slash ("/").
   */
  private static String normalizeKey(String key) {
    String normalized = key.replaceAll("/{2,}", "/");
    if (normalized.charAt(0) != '/') {
      normalized = "/" + normalized; //NOPMD
    }
    return normalized;
  }

  /**
   * Creates, updates, or deletes tree nodes in the network table view.
   *
   * @param fullKey   the network table key that changed
   * @param value the value of the entry that changed
   * @param flags the flags of the change
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void makeBranches(String fullKey, Object value, int flags) {
    String key = normalizeKey(fullKey);
    boolean deleted = (flags & ITable.NOTIFY_DELETE) != 0;
    List<String> pathElements = Stream.of(key.split("/"))
                                      .filter(s -> !s.isEmpty())
                                      .collect(Collectors.toList());
    TreeItem<NetworkTableEntry> current = networktableRoot;
    TreeItem<NetworkTableEntry> parent;
    StringBuilder currentKey = new StringBuilder();
    // Add, remove or update nodes in the tree as necessary
    for (int i = 0; i < pathElements.size(); i++) {
      String pathElement = pathElements.get(i);
      currentKey.append('/').append(pathElement);
      parent = current;
      current = current.getChildren().stream()
                       .filter(item -> item.getValue().getKey().equals(currentKey.toString()))
                       .findFirst()
                       .orElse(null);
      if (deleted) {
        if (current == null) {
          // Nothing to remove
          break;
        } else if (i == pathElements.size() - 1) {
          // Remove the final node
          parent.getChildren().remove(current);
        }
      } else if (i == pathElements.size() - 1) {
        // At the end
        if (current == null) {
          // Newly added value, create a tree item for it
          current = new TreeItem<>(new NetworkTableEntry(key, asString(value)));
          parent.getChildren().add(current);
        } else {
          // The value updated, so just update the previous node
          current.getValue().setValue(asString(value));
        }
      } else if (current == null) {
        // It's a branch (subtable); expand it
        current = new TreeItem<>(new NetworkTableEntry(currentKey.toString(), ""));
        current.setExpanded(true);
        parent.getChildren().add(current);
      }
    }
    Platform.runLater(this::refreshTableView);
  }

  private void refreshTableView() {
    keyColumn.setVisible(false);
    keyColumn.setVisible(true);
  }

  private String asString(Object o) {
    if (o instanceof double[]) {
      return Arrays.toString((double[]) o);
    }
    if (o instanceof String[]) {
      return Arrays.toString((String[]) o);
    }
    if (o instanceof boolean[]) {
      return Arrays.toString((boolean[]) o);
    }
    return o.toString();
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
    double w = Math.max(colWidth, view.getPrefWidth());
    double h = Math.max(rowHeight, view.getPrefHeight());
    Size size = new Size((int) (w / colWidth), (int) (h / rowHeight));
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
    Node uiElement = addTile(control, size);
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


  // Tile stuff
  // TODO move all this to its own class

  private Node addTile(Node node, Size size) {
    return addTile(node, size.getWidth(), size.getHeight());
  }

  /**
   * Adds a node in the first available spot. The node will be wrapped in a pane to make it
   * easier to add single controls (buttons, labels, etc). This will fail (return {@code null}) iff:
   * <ul>
   * <li>{@code node} is {@code null}; or</li>
   * <li>{@code width} is zero or negative; or</li>
   * <li>{@code height} is zero or negative; or</li>
   * <li>there is no available space for a tile with the given dimensions</li>
   * </ul>
   *
   * @param node   the node to add
   * @param width  the width of the tile for the node. Must be >= 1
   * @param height the height of the tile for the node. Must be >= 1
   * @return the node added to the view
   */
  private Node addTile(Node node, int width, int height) {
    if (node == null) {
      // Can't add a null tile
      return null;
    }
    if (width < 1 || height < 1) {
      // Illegal dimensions
      return null;
    }
    Point placement = firstPoint(width, height);
    if (placement == null) {
      // Nowhere to place the node
      return null;
    }

    StackPane wrapper = new StackPane(node);
    wrapper.getStyleClass().add("tile");

    tileGrid.add(wrapper, placement.col, placement.row, width, height);
    return wrapper;
  }

  /**
   * Finds the first point where a tile with the given dimensions can be added,
   * or {@code null} if no such point exists.
   *
   * @param width  the width of the tile trying to be added
   * @param height the height of the tile trying to be added
   */
  private Point firstPoint(int width, int height) {
    // outer row, inner col to add tiles left-to-right in the upper rows
    // outer col, inner row would add tiles top-to-bottom from the left-hand columns (not intuitive)
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        if (isOpen(col, row, width, height)) {
          return new Point(col, row);
        }
      }
    }
    return null;
  }

  /**
   * Checks if a tile with the given width and height can be added at the point {@code (col, row)}.
   *
   * @param col        the column index of the point to check
   * @param row        the row index of the point to check
   * @param tileWidth  the width of the tile
   * @param tileHeight the height of the tile
   */
  private boolean isOpen(int col, int row, int tileWidth, int tileHeight) {
    if (col + tileWidth > numCols || row + tileHeight > numRows) {
      return false;
    }

    int x;
    int y;
    int width;
    int height;

    for (Node tile : tileGrid.getChildren()) {
      try {
        x = GridPane.getColumnIndex(tile);
        y = GridPane.getRowIndex(tile);
        width = GridPane.getColumnSpan(tile);
        height = GridPane.getRowSpan(tile);
      } catch (NullPointerException e) {
        // Not a real child (Geppetto pls)
        continue;
      }

      if (x + width > col && y + height > row
          && x < col + tileWidth && y < row + tileHeight) {
        // Check intersection
        return false;
      }

    }

    return true;
  }

}
