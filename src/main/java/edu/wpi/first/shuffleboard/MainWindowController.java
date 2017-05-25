package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.NetworkTableTree;
import edu.wpi.first.shuffleboard.components.TilePane;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.NetworkTableSource;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;
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
  private TilePane tileGrid;
  @FXML
  private NetworkTableTree networkTables;

  private final ITable rootTable = NetworkTable.getTable("");

  // Keep track of the widgets and corresponding UI components
  private final List<WidgetHandle> widgetHandles = new ArrayList<>();

  private static final PseudoClass selectedPseudoClass = PseudoClass.getPseudoClass("selected");

  private static final class WidgetHandle {
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
      }
    });

    networkTables.getSelectionModel()
                 .selectedItemProperty()
                 .addListener((__, __o, newValue) -> {
                   if (newValue == null) {
                     deselectAllWidgets();
                   }
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
