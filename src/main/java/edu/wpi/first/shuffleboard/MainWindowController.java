package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.NetworkTableTree;
import edu.wpi.first.shuffleboard.components.TilePane;
import edu.wpi.first.shuffleboard.dnd.DataSourceTransferable;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.shuffleboard.widget.TileSize;
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
import javafx.scene.control.TreeTableRow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
  private TilePane tileGrid;
  @FXML
  private NetworkTableTree networkTables;

  private final ITable rootTable = NetworkTable.getTable("");

  // Keep track of the widgets and corresponding UI components
  private final List<WidgetHandle> widgetHandles = new ArrayList<>();

  private static final PseudoClass selectedPseudoClass = PseudoClass.getPseudoClass("selected");

  private static final DataFormat dataSourceFormat = new DataFormat("shuffleboard/data-source");
  private static final DataFormat widgetFormat = new DataFormat("shuffleboard/widget");

  /**
   * Allows us to keep track of widgets and their UI elements.
   */
  private static final class WidgetHandle {
    /**
     * A unique string used to identify this handle.
     */
    private final String id = UUID.randomUUID().toString();
    private final Widget widget;
    private TileSize currentSize;
    private String sourceName;
    private Node uiElement;

    public WidgetHandle(Widget widget) {
      this.widget = widget;
      sourceName = widget.getSourceName();
    }

    public Widget getWidget() {
      return widget;
    }

    public TileSize getCurrentSize() {
      return currentSize;
    }

    public void setCurrentSize(TileSize currentSize) {
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
  private void initialize() throws IOException {
    // NetworkTable view init
    networkTables.getKeyColumn().setPrefWidth(199);
    networkTables.getValueColumn().setPrefWidth(199);

    Pane gridHighlight = new StackPane();
    gridHighlight.getStyleClass().add("grid-highlight");

    // Drag and drop sources onto the tile grid or drag widgets around
    tileGrid.setOnDragOver(event -> {
      event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      tileGrid.setGridLinesVisible(true);
      tileGrid.getChildren().remove(gridHighlight);
      boolean isWidget = event.getDragboard().hasContent(widgetFormat);

      // preview the location of the widget if one is being dragged
      if (isWidget) {
        String widgetId = (String) event.getDragboard().getContent(widgetFormat);
        widgetHandles.stream()
                     .filter(handle -> handle.id.equals(widgetId))
                     .findFirst()
                     .ifPresent(handle -> {
                       TileSize size = handle.getCurrentSize();
                       GridPoint origin = tileGrid.pointAt(event.getX(), event.getY());
                       tileGrid.add(gridHighlight,
                                    origin.col, origin.row,
                                    size.getWidth(), size.getHeight());
                     });
      }
      // setting grid lines visible puts them above every child, so move every widget view
      // to the front to avoid them being obscure by the grid lines
      widgetHandles.stream()
                   .map(WidgetHandle::getUiElement)
                   .forEach(Node::toFront);
      event.consume();
    });

    tileGrid.setOnDragDone(__ -> cleanupWidgetDrag(gridHighlight));
    tileGrid.setOnDragExited(__ -> cleanupWidgetDrag(gridHighlight));

    // Drop sources or widgets into the tile grid
    tileGrid.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      GridPoint point = tileGrid.pointAt(event.getX(), event.getY());
      if (dragboard.hasContent(dataSourceFormat)) {
        DataSourceTransferable sourceTransferable =
            (DataSourceTransferable) dragboard.getContent(dataSourceFormat);

        DataSource<?> source = sourceTransferable.createSource();
        Widgets.widgetNamesForSource(source)
               .stream()
               .findAny()
               .flatMap(name -> Widgets.createWidget(name, source))
               .ifPresent(this::addWidget);
        widgetHandles.stream()
                     .filter(handle -> handle.getWidget().getSource() == source) // intentional ==
                     .map(WidgetHandle::getUiElement)
                     .findAny()
                     .ifPresent(node -> tileGrid.setLocation(node, point));

      } else if (dragboard.hasContent(widgetFormat)) {
        String widgetId = (String) dragboard.getContent(widgetFormat);
        widgetHandles.stream()
                     .filter(handle -> widgetId.equals(handle.id))
                     .map(WidgetHandle::getUiElement)
                     .findAny()
                     .ifPresent(node -> tileGrid.setLocation(node, point));
      }
      cleanupWidgetDrag(gridHighlight);
      event.consume();
    });

    networkTables.setRowFactory(view -> {
      TreeTableRow<NetworkTableEntry> row = new TreeTableRow<>();
      row.hoverProperty().addListener((__, wasHover, isHover) -> {
        if (!row.isEmpty()) {
          highlight(row.getTreeItem(), isHover);
        }
      });

      // drag sources into the tile grid
      row.setOnDragDetected(event -> {
        if (!row.isEmpty()) {
          NetworkTableEntry entry = row.getTreeItem().getValue();
          Dragboard dragboard = row.startDragAndDrop(TransferMode.COPY_OR_MOVE);
          ClipboardContent content = new ClipboardContent();
          content.put(dataSourceFormat, DataSourceTransferable.networkTable(entry.getKey()));
          dragboard.setContent(content);
        }
        event.consume();
      });
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

  /**
   * Cleans up from dragging widgets around in the tile pane.
   *
   * @param gridHighlight the node used to highlight the drop target location
   */
  private void cleanupWidgetDrag(Node gridHighlight) {
    tileGrid.setGridLinesVisible(false);
    tileGrid.getChildren().remove(gridHighlight);
  }

  private MenuItem createShowAsMenuItem(String widgetName, DataSource<?> source) {
    MenuItem menuItem = new MenuItem("Show as: " + widgetName);
    menuItem.setOnAction(action -> {
      Widgets.createWidget(widgetName, source)
             .ifPresent(this::addWidget);
    });
    return menuItem;
  }

  private void highlight(TreeItem<NetworkTableEntry> node, boolean doHighlight) {
    findWidgets(node.getValue().getKey())
        .forEach(handle -> highlight(handle.getUiElement(), doHighlight));

    if (!node.isLeaf()) {
      // Highlight all child widgets
      widgetHandles.stream()
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
    widgetHandles.stream()
                 .map(WidgetHandle::getUiElement)
                 .forEach(node -> highlight(node, false));
  }

  /**
   * Finds all widgets in the tile grid that are associated with the given network table key.
   */
  private List<WidgetHandle> findWidgets(String fullTableKey) {
    String key = NetworkTableUtils.normalizeKey(fullTableKey, false);
    return widgetHandles.stream()
                        .filter(handle -> handle.sourceName.equals(key))
                        .collect(Collectors.toList());
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
    double width = Math.max(tileGrid.getTileSize(), view.getPrefWidth());
    double height = Math.max(tileGrid.getTileSize(), view.getPrefHeight());

    TileSize size = new TileSize((int) (width / tileGrid.getTileSize()),
                                 (int) (height / tileGrid.getTileSize()));
    addWidget(widget, size);
  }

  /**
   * Adds a widget to the tile view in the first available location. The tile will be the specified
   * size.
   *
   * @param widget the widget to add
   * @param size   the size of the tile used to display the widget
   */
  public void addWidget(Widget<?> widget, TileSize size) {
    WidgetHandle handle = new WidgetHandle(widget);
    handle.setCurrentSize(size);
    widgetHandles.add(handle);
    Pane control = widget.getView();
    Node uiElement = tileGrid.addTile(control, size);
    uiElement.setOnDragDetected(event -> {
      Dragboard dragboard = uiElement.startDragAndDrop(TransferMode.MOVE);
      WritableImage preview =
          new WritableImage(
              (int) uiElement.getBoundsInParent().getWidth(),
              (int) uiElement.getBoundsInParent().getHeight()
          );
      uiElement.snapshot(null, preview);
      dragboard.setDragView(preview);
      ClipboardContent content = new ClipboardContent();
      content.put(widgetFormat, handle.id);
      dragboard.setContent(content);
      event.consume();
    });
    handle.setUiElement(uiElement);
    control.setOnContextMenuRequested(e -> {
      ContextMenu menu = createContextMenu(handle);
      menu.show(root.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
    handle.getUiElement().pseudoClassStateChanged(selectedPseudoClass, true);
  }

  /**
   * Removes a widget from the view.
   *
   * @param widget the widget to remove
   */
  public void removeWidget(Widget<?> widget) {
    widgetHandles.stream()
                 .filter(h -> h.getWidget() == widget)
                 .findFirst()
                 .ifPresent(this::removeWidget);
  }

  /**
   * Removes a widget from the view.
   *
   * @param handle the handle for the widget to remove
   */
  private void removeWidget(WidgetHandle handle) {
    tileGrid.getChildren().remove(handle.getUiElement());
    widgetHandles.remove(handle);
  }

  private ContextMenu createContextMenu(WidgetHandle handle) {
    ContextMenu menu = new ContextMenu();
    MenuItem remove = new MenuItem("Remove");
    remove.setOnAction(__ -> removeWidget(handle));
    menu.getItems().addAll(createChangeMenus(handle), new SeparatorMenuItem(), remove);
    return menu;
  }

  private MenuItem createChangeMenus(WidgetHandle handle) {
    Widget<?> widget = handle.getWidget();
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
               changeItem.setOnAction(__ -> {
                 // TODO this has a lot of room for improvement
                 removeWidget(handle);
                 Widgets.createWidget(name, widget.getSource())
                        .ifPresent(this::addWidget);
               });
             }
             changeView.getItems().add(changeItem);
           });
    return changeView;
  }

}
