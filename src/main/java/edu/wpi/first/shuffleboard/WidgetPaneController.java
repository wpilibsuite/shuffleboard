package edu.wpi.first.shuffleboard;


import edu.wpi.first.shuffleboard.components.TileLayout;
import edu.wpi.first.shuffleboard.components.WidgetPane;
import edu.wpi.first.shuffleboard.components.WidgetTile;
import edu.wpi.first.shuffleboard.dnd.DataFormats;
import edu.wpi.first.shuffleboard.dnd.TileDragResizer;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.util.RoundingMode;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;

import org.fxmisc.easybind.EasyBind;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.binding.Binding;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

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
        DataFormats.WidgetData data = (DataFormats.WidgetData) event.getDragboard().getContent(DataFormats.widgetTile);
        pane.tileMatching(tile -> tile.getId().equals(data.getId()))
            .ifPresent(tile -> previewWidget(tile, point.subtract(data.getDragPoint())));
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
        DataFormats.WidgetData data = (DataFormats.WidgetData) dragboard.getContent(DataFormats.widgetTile);
        pane.tileMatching(tile -> tile.getId().equals(data.getId()))
            .ifPresent(tile -> moveWidget(tile, point.subtract(data.getDragPoint())));
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

    pane.parentProperty().addListener((__, old, parent) -> {
      if (parent instanceof Region) {
        Region region = (Region) parent;
        Binding<Integer> colBinding =
            EasyBind.combine(region.widthProperty(), pane.hgapProperty(), pane.tileSizeProperty(),
                (width, gap, size) -> pane.roundWidthToNearestTile(width.doubleValue(), RoundingMode.DOWN))
                .map(numCols -> Math.max(1, numCols));
        Binding<Integer> rowBinding =
            EasyBind.combine(region.heightProperty(), pane.vgapProperty(), pane.tileSizeProperty(),
                (height, gap, size) -> pane.roundHeightToNearestTile(height.doubleValue(), RoundingMode.DOWN))
                .map(numRows -> Math.max(numRows, 1));


        pane.numColumnsProperty().bind(colBinding);
        pane.numRowsProperty().bind(rowBinding);
      }
    });

    pane.numColumnsProperty().addListener((__, oldCount, newCount) -> {
      if (pane.getTiles().isEmpty()) {
        // No tiles, bail
        return;
      }
      if (newCount < oldCount) {
        // shift and shrink tiles to the left
        pane.getTiles().stream()
            .filter(tile -> {
              final TileLayout layout = pane.getTileLayout(tile);
              return layout.origin.col + layout.size.getWidth() > newCount;
            })
            .forEach(tile -> moveTileLeft(tile, oldCount - newCount));
      }
    });
    pane.numRowsProperty().addListener((__, oldCount, newCount) -> {
      if (pane.getTiles().isEmpty()) {
        return;
      }
      if (newCount < oldCount) {
        // shift and shrink tiles up
        pane.getTiles().stream()
            .filter(tile -> {
              final TileLayout layout = pane.getTileLayout(tile);
              return layout.origin.row + layout.size.getHeight() > newCount;
            })
            .forEach(tile -> moveTileUp(tile, oldCount - newCount));
      }
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
  private void dragWidget(WidgetTile tile, GridPoint point) {
    Dragboard dragboard = tile.startDragAndDrop(TransferMode.MOVE);
    WritableImage preview =
        new WritableImage(
            (int) tile.getBoundsInParent().getWidth(),
            (int) tile.getBoundsInParent().getHeight()
        );
    tile.snapshot(null, preview);
    dragboard.setDragView(preview);
    ClipboardContent content = new ClipboardContent();
    content.put(DataFormats.widgetTile, new DataFormats.WidgetData(tile.getId(), point));
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

    TileDragResizer resizer = TileDragResizer.makeResizable(pane, tile);

    tile.setOnDragDetected(event -> {
      if (resizer.isDragging()) {
        // don't drag the widget while it's being resized
        return;
      }
      GridPoint dragPoint = new GridPoint(pane.roundWidthToNearestTile(event.getX()) - 1,
          pane.roundHeightToNearestTile(event.getY()) - 1);
      dragWidget(tile, dragPoint);
      event.consume();
    });

    tile.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      if (dragboard.hasContent(DataFormats.source)) {
        SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);
        tile.getWidget().setSource(entry.get());
        event.consume();
      }
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
    Menu changeMenus = createChangeMenus(tile);
    if (changeMenus.getItems().size() > 1) {
      menu.getItems().addAll(changeMenus, new SeparatorMenuItem());
    }
    menu.getItems().add(remove);
    return menu;
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   *
   * @param tile the tile for the widget to create the change menus for
   */
  private Menu createChangeMenus(WidgetTile tile) {
    Widget widget = tile.getWidget();
    Menu changeView = new Menu("Show as...");
    Widgets.widgetNamesForType(widget.getSource().getDataType())
           .stream()
           .sorted()
           .forEach(name -> {
             MenuItem changeItem = new MenuItem(name);
             if (name.equals(widget.getName())) {
               changeItem.setGraphic(new Label("✓"));
             } else {
               // only need to change if it's to another type
               changeItem.setOnAction(__ -> {
                 Widgets.createWidget(name, widget.getSource())
                        .ifPresent(tile::setWidget);
               });
             }
             changeView.getItems().add(changeItem);
           });
    return changeView;
  }

  private void moveTileLeft(WidgetTile tile, int count) {
    for (int i = 0; i < count; i++) {
      Optional<Runnable> move = moveTileLeft(tile);
      if (move.isPresent()) {
        move.get().run();
      } else {
        // Can't move any further, bail
        break;
      }
    }
  }

  private Optional<Runnable> moveTileLeft(WidgetTile tile) {
    TileLayout layout = pane.getTileLayout(tile);
    final int previousColumn = layout.origin.col - 1;
    if (!pane.isOverlapping(previousColumn, layout.origin.row,
        layout.size.getWidth(), layout.size.getHeight(), n -> tile == n) && previousColumn >= 0) {
      // Great, we can move it
      return Optional.of(() -> GridPane.setColumnIndex(tile, previousColumn));
    } else if (layout.origin.col > 0) {
      // Check to see if we can move any tiles to the left, if possible
      List<Runnable> runs = IntStream.range(layout.origin.row, layout.origin.row + layout.size.getHeight())
          .mapToObj(row -> pane.tileAt(previousColumn, row))
          .filter(Optional::isPresent) // guaranteed to be at least one tile
          .map(Optional::get)
          .distinct() // need to make sure we have no repeats, or n-row tiles will get moved n times
          .filter(t -> tile != t)
          .map(this::moveTileLeft) // recursion here
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
      if (runs.isEmpty()) {
        // Can't move or shrink any tiles to the left of this one to make it move
        return tryShrinkTile(tile, t -> t.getSize().getWidth(), t -> new TileSize(t.getWidth() - 1, t.getHeight()));
      } else {
        return Optional.of(() -> {
          runs.forEach(Runnable::run);
          GridPane.setColumnIndex(tile, previousColumn);
        });
      }
    } else {
      return tryShrinkTile(tile, t -> t.getSize().getWidth(), t -> new TileSize(t.getWidth() - 1, t.getHeight()));
    }
  }

  private void moveTileUp(WidgetTile tile, int count) {
    for (int i = 0; i < count; i++) {
      Optional<Runnable> move = moveTileUp(tile);
      if (move.isPresent()) {
        move.get().run();
      } else {
        // Can't move any further, bail
        break;
      }
    }
  }

  private Optional<Runnable> moveTileUp(WidgetTile tile) {
    TileLayout layout = pane.getTileLayout(tile);
    final int previousRow = layout.origin.row - 1;
    if (!pane.isOverlapping(layout.origin.col, previousRow,
        layout.size.getWidth(), layout.size.getHeight(), n -> tile == n) && previousRow >= 0) {
      // Great, we can move it
      return Optional.of(() -> GridPane.setRowIndex(tile, previousRow));
    } else if (layout.origin.row > 0) {
      // Check to see if we can move any tiles up, if possible
      List<Runnable> runs = IntStream.range(layout.origin.col, layout.origin.col + layout.size.getWidth())
          .mapToObj(col -> pane.tileAt(col, previousRow))
          .filter(Optional::isPresent) // guaranteed to be at least one tile
          .map(Optional::get)
          .distinct() // need to make sure we have no repeats, or n-row tiles will get moved n times
          .filter(t -> tile != t)
          .map(this::moveTileUp) // recursion here
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList());
      if (runs.isEmpty()) {
        // Can't move or shrink any tiles to the left of this one to make it move
        return tryShrinkTile(tile, t -> t.getSize().getHeight(), t -> new TileSize(t.getWidth(), t.getHeight() - 1));
      } else {
        return Optional.of(() -> {
          runs.forEach(Runnable::run);
          GridPane.setRowIndex(tile, previousRow);
        });
      }
    } else {
      return tryShrinkTile(tile, t -> t.getSize().getHeight(), t -> new TileSize(t.getWidth(), t.getHeight() - 1));
    }
  }

  private Optional<Runnable> tryShrinkTile(WidgetTile tile,
                                           ToIntFunction<WidgetTile> dim,
                                           Function<TileSize, TileSize> resize) {
    if (dim.applyAsInt(tile) > 1) {
      return Optional.of(() -> tile.setSize(resize.apply(tile.getSize())));
    } else {
      return Optional.empty();
    }
  }

}
