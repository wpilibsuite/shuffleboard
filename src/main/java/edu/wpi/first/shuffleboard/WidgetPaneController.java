package edu.wpi.first.shuffleboard;


import edu.wpi.first.shuffleboard.components.TileLayout;
import edu.wpi.first.shuffleboard.components.WidgetPane;
import edu.wpi.first.shuffleboard.components.WidgetTile;
import edu.wpi.first.shuffleboard.dnd.DataFormats;
import edu.wpi.first.shuffleboard.dnd.TileDragResizer;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.util.RoundingMode;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;

import org.fxmisc.easybind.EasyBind;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
      boolean isSource = event.getDragboard().hasContent(DataFormats.source);

      // preview the location of the widget if one is being dragged
      if (isWidget) {
        pane.setHighlight(true);
        pane.setHighlightPoint(point);
        DataFormats.WidgetData data = (DataFormats.WidgetData) event.getDragboard().getContent(DataFormats.widgetTile);
        pane.tileMatching(tile -> tile.getId().equals(data.getId()))
            .ifPresent(tile -> previewWidget(tile, point.subtract(data.getDragPoint())));
      } else if (isSource) {
        SourceEntry entry = (SourceEntry) event.getDragboard().getContent(DataFormats.source);
        DataSource source = entry.get();
        List<String> names = Widgets.widgetNamesForSource(source);
        Optional<DummySource> dummySource = DummySource.forTypes(source.getDataType());
        if (!names.isEmpty() && dummySource.isPresent()) {
          Widgets.createWidget(names.get(0), (DataSource<?>) dummySource.get()).ifPresent(w -> {
            pane.setHighlight(true);
            pane.setHighlightPoint(point);
            pane.setHighlightSize(pane.sizeOfWidget(w));
          });
        }
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
            .forEach(tile -> moveTile(tile, oldCount - newCount, true));
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
            .forEach(tile -> moveTile(tile, oldCount - newCount, false));
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
           .filter(widget -> pane.isOpen(point, pane.sizeOfWidget(widget), n -> widget == n))
           .map(pane::addWidget)
           .ifPresent(tile -> pane.moveNode(tile, point));
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
    MenuItem remove = FxUtils.menuItem("Remove", __ -> pane.removeWidget(tile));
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

  private void moveTile(WidgetTile tile, int count, boolean left) {
    Function<TileLayout, TileLayout> moveLeft = l -> l.withCol(l.origin.col - 1);
    Function<TileLayout, TileLayout> moveUp = l -> l.withRow(l.origin.row - 1);
    Function<TileSize, TileSize> shrinkLeft = s -> new TileSize(Math.max(1, s.getWidth() - 1), s.getHeight());
    Function<TileSize, TileSize> shrinkUp = s -> new TileSize(s.getWidth(), Math.max(1, s.getHeight() - 1));
    for (int i = 0; i < count; i++) {
      Optional<Runnable> move;
      if (left) {
        move = moveTile(tile, moveLeft, shrinkLeft);
      } else {
        move = moveTile(tile, moveUp, shrinkUp);
      }
      if (move.isPresent()) {
        move.get().run();
      } else {
        // Can't move any further, bail
        break;
      }
    }
  }

  /**
   * Creates a {@link Runnable} that will move or shrink the given tile, as well as moving or shrinking any tiles
   * in the way of moving it. If the tile cannot be shrunk or moved, returns an empty Optional.
   *
   * @param tile                 the tile to move
   * @param targetLayoutFunction the function to use to set the origin for the target location
   * @param shrink               the function to use to shrink the tile
   */
  private Optional<Runnable> moveTile(WidgetTile tile,
                                      Function<TileLayout, TileLayout> targetLayoutFunction,
                                      Function<TileSize, TileSize> shrink) {
    TileLayout layout = pane.getTileLayout(tile);
    TileLayout targetLayout = targetLayoutFunction.apply(layout);
    boolean left = targetLayout.origin.col < layout.origin.col;
    int importantDim = left ? layout.size.getWidth() : layout.size.getHeight();
    if (!pane.isOverlapping(targetLayout, n -> n == tile) && !targetLayout.origin.equals(layout.origin)) { // NOPMD
      // Great, we can move it
      return Optional.of(() -> {
        GridPane.setColumnIndex(tile, targetLayout.origin.col);
        GridPane.setRowIndex(tile, targetLayout.origin.row);
      });
    } else if (importantDim > 1) {
      // Shrink the tile
      return Optional.of(() -> tile.setSize(shrink.apply(tile.getSize())));
    } else if (!targetLayout.origin.equals(layout.origin)) { // NOPMD
      // Try to move or shrink other tiles in the way, then move this one into the free space
      int lower = left ? layout.origin.row : layout.origin.col;
      int upper = lower + importantDim;
      List<Optional<Runnable>> runs = IntStream.range(lower, upper)
          .mapToObj(i -> left ? pane.tileAt(targetLayout.origin.col, i) : pane.tileAt(i, targetLayout.origin.row))
          .filter(Optional::isPresent) // guaranteed to be at least one tile
          .map(Optional::get)
          .distinct() // need to make sure we have no repeats, or n-row tiles will get moved n times
          .filter(t -> tile != t)
          .map(t -> moveTile(t, targetLayoutFunction, shrink)) // recursion here
          .collect(Collectors.toList());
      if (runs.stream().allMatch(Optional::isPresent)) {
        return Optional.of(() -> {
          runs.forEach(r -> r.get().run());
          GridPane.setColumnIndex(tile, targetLayout.origin.col);
          GridPane.setRowIndex(tile, targetLayout.origin.row);
        });
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

}
