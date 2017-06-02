package edu.wpi.first.shuffleboard;


import edu.wpi.first.shuffleboard.components.WidgetPane;
import edu.wpi.first.shuffleboard.dnd.DataFormats;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class WidgetPaneController {

  @FXML
  private WidgetPane pane;

  @FXML
  private void initialize() {

    pane.getTiles().addListener((ListChangeListener<WidgetTile>) changes -> {
      while (changes.next()) {
        changes.getAddedSubList().forEach(this::setupTile);
      }
    });

    // Handle being dragged over
    pane.setOnDragOver(event -> {
      event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      pane.setGridLinesVisible(true);
      GridPoint point = pane.pointAt(event.getX(), event.getY());
      boolean isWidget = event.getDragboard().hasContent(DataFormats.widgetTile);

      // preview the location of the widget if one is being dragged
      if (isWidget) {
        pane.setHighlight(true);
        pane.setHighlightPoint(point);
        String widgetId = (String) event.getDragboard().getContent(DataFormats.widgetTile);
        pane.tileMatching(tile -> tile.getId().equals(widgetId))
            .ifPresent(tile -> previewWidget(tile, point));
      }

      // setting grid lines visible puts them above every child, so move every widget view
      // to the front to avoid them being obscure by the grid lines
      // this is a limitation of the JavaFX API that we have to work around
      pane.getTiles().forEach(Node::toFront);
      event.consume();
    });

    // Clean up widget drags when the drag exits the pane
    pane.setOnDragDone(__ -> cleanupWidgetDrag());
    pane.setOnDragExited(__ -> cleanupWidgetDrag());

    // Handle dropping stuff onto the pane
    pane.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      GridPoint point = pane.pointAt(event.getX(), event.getY());
      if (dragboard.hasContent(DataFormats.source)) {
        SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);
        dropSource(entry.get(), point);
      }

      if (dragboard.hasContent(DataFormats.widgetTile)) {
        String widgetId = (String) dragboard.getContent(DataFormats.widgetTile);
        pane.tileMatching(tile -> tile.getId().equals(widgetId))
            .ifPresent(tile -> moveWidget(tile, point));
      }

      if (dragboard.hasContent(DataFormats.widgetType)) {
        String widgetType = (String) dragboard.getContent(DataFormats.widgetType);
        Widgets.typeFor(widgetType).ifPresent(type -> {
          Widget widget = type.get();
          TileSize size = pane.sizeOfWidget(widget);
          if (pane.isOpen(point, size, _t -> false)) {
            WidgetTile tile = pane.addWidget(widget);
            moveWidget(tile, point);
          }
        });
      }

      cleanupWidgetDrag();
      event.consume();
    });
  }

  /**
   * Cleans up from dragging widgets around in the tile pane.
   */
  private void cleanupWidgetDrag() {
    pane.setGridLinesVisible(false);
    pane.setHighlight(false);
  }

  /**
   * Starts the drag of the given widget tile.
   */
  private void dragWidget(WidgetTile tile) {
    Dragboard dragboard = tile.startDragAndDrop(TransferMode.MOVE);
    WritableImage preview =
        new WritableImage(
            (int) tile.getBoundsInParent().getWidth(),
            (int) tile.getBoundsInParent().getHeight()
        );
    tile.snapshot(null, preview);
    dragboard.setDragView(preview);
    ClipboardContent content = new ClipboardContent();
    content.put(DataFormats.widgetTile, tile.getId());
    dragboard.setContent(content);
  }

  /**
   * Drops a widget into this pane at the given point.
   *
   * @param tile  the tile for the widget to drop
   * @param point the point in the tile pane to drop the widget at
   */
  private void moveWidget(WidgetTile tile, GridPoint point) {
    TileSize size = tile.getSize();
    if (pane.isOpen(point, size, n -> n == tile)) {
      pane.moveNode(tile, point);
    }
  }

  /**
   * Drops a data source into the tile pane at the given point. The source will be displayed
   * with the default widget for its data type. If there is no default widget for that data,
   * then no widget will be created.
   *
   * @param source the source to drop
   * @param point  the point to place the widget for the source
   */
  private void dropSource(DataSource<?> source, GridPoint point) {
    Widgets.widgetNamesForSource(source)
           .stream()
           .findAny()
           .flatMap(name -> Widgets.createWidget(name, source))
           .map(pane::addWidget);
    pane.widgetForSource(source)
        .ifPresent(node -> pane.moveNode(node, point));
  }

  /**
   * Sets up a tile with a context menu and lets it be dragged around.
   */
  private void setupTile(WidgetTile tile) {
    tile.setOnContextMenuRequested(event -> {
      ContextMenu contextMenu = createContextMenu(tile);
      contextMenu.show(pane.getScene().getWindow(), event.getScreenX(), event.getScreenY());
    });
    tile.setOnDragDetected(event -> {
      dragWidget(tile);
      event.consume();
    });
  }

  /**
   * Previews the widget for the given tile.
   *
   * @param tile  the tile for the widget to preview
   * @param point the point to preview the widget at
   */
  private void previewWidget(WidgetTile tile, GridPoint point) {
    TileSize size = tile.getSize();
    pane.setHighlightPoint(point);
    pane.setHighlightSize(size);
  }

  /**
   * Creates the context menu for a widget.
   *
   * @param tile the tile for the widget to create a context menu for
   */
  private ContextMenu createContextMenu(WidgetTile tile) {
    ContextMenu menu = new ContextMenu();
    MenuItem remove = new MenuItem("Remove");
    remove.setOnAction(__ -> pane.removeWidget(tile));
    menu.getItems().addAll(createChangeMenus(tile), new SeparatorMenuItem(), remove);
    return menu;
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   *
   * @param tile the tile for the widget to create the change menus for
   */
  private MenuItem createChangeMenus(WidgetTile tile) {
    Widget widget = tile.getWidget();
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
                 pane.removeWidget(tile);
                 Widgets.createWidget(name, widget.getSource())
                        .ifPresent(pane::addWidget);
               });
             }
             changeView.getItems().add(changeItem);
           });
    return changeView;
  }

}
