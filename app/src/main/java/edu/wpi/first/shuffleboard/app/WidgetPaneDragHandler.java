package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats.TilelessComponentData;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.TileLayout;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * Helper class for handling drag events on a widget pane.
 */
@SuppressWarnings("PMD.GodClass") // seriously?
final class WidgetPaneDragHandler implements EventHandler<DragEvent> {

  private final WidgetPane pane;
  private final TileSelector selector;

  /**
   * Memoizes the size of a tile that would be added when dropping a source or widget. Memoizing prevents calling
   * potentially expensive component initialization code every time the mouse moves when previewing the location of a
   * tile for a source or widget.
   */
  private TileSize tilePreviewSize = null;

  private final Map<Tile<?>, WidgetPane.Highlight> highlights = new WeakHashMap<>();

  WidgetPaneDragHandler(WidgetPane pane) {
    this.pane = pane;
    this.selector = TileSelector.forPane(pane);
  }

  private boolean isLayoutTile(GridPoint point) {
    return pane.tileAt(point)
        .map(Tile::getContent)
        .filter(c -> c instanceof Layout)
        .isPresent();
  }

  @Override
  public void handle(DragEvent event) {
    EventType<DragEvent> eventType = event.getEventType();
    if (eventType == DragEvent.DRAG_OVER) {
      handleDragOver(event);
    } else if (eventType == DragEvent.DRAG_DROPPED) {
      handleDragDropped(event);
    } else if (eventType == DragEvent.DRAG_DONE || eventType == DragEvent.DRAG_EXITED) {
      cleanupWidgetDrag();
    }
  }

  /**
   * Handles a DRAG_OVER event on the widget pane.
   *
   * @param event the drag event.
   */
  private void handleDragOver(DragEvent event) {
    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    GridPoint point = pane.pointAt(event.getX(), event.getY());
    Dragboard dragboard = event.getDragboard();

    // Preview the location of a single tile being dragged
    if (dragboard.hasContent(DataFormats.singleTile) && !previewSingleTile(event, point, dragboard)) {
      return;
    }

    // Preview the locations of multiple tiles being dragged
    if (dragboard.hasContent(DataFormats.multipleTiles) && !previewManyTiles(point, dragboard)) {
      return;
    }
    if (dragboard.hasContent(DataFormats.source) && !previewSource(point, dragboard)) {
      return;
    }
    if (dragboard.hasContent(DataFormats.widgetType) && !previewGalleryWidget(point, dragboard)) {
      return;
    }
    if (dragboard.hasContent(DataFormats.tilelessComponent) && !previewTilelessComponent(point, dragboard)) {
      return;
    }

    event.consume();
  }

  private boolean previewTilelessComponent(GridPoint point, Dragboard dragboard) {
    if (!pane.isOpen(point, new TileSize(1, 1), n -> false)) {
      // Dragged a widget onto a tile, can't drop
      pane.setHighlight(false);
      return false;
    }
    Optional<Component> component = Components.getDefault()
        .getByUuid(((TilelessComponentData) dragboard.getContent(DataFormats.tilelessComponent)).getComponentId());
    if (tilePreviewSize == null) {
      component.map(pane::sizeOfWidget)
          .ifPresent(size -> tilePreviewSize = size);
    }
    highlightPoint(point);
    return true;
  }

  private boolean previewGalleryWidget(GridPoint point, Dragboard dragboard) {
    if (!pane.isOpen(point, new TileSize(1, 1), n -> false)) {
      // Dragged a widget onto a tile, can't drop
      pane.setHighlight(false);
      return false;
    }
    String componentType = (String) dragboard.getContent(DataFormats.widgetType);
    if (tilePreviewSize == null) {
      Components.getDefault().createComponent(componentType)
          .map(pane::sizeOfWidget)
          .ifPresent(size -> tilePreviewSize = size);
    }
    highlightPoint(point);
    return true;
  }

  private boolean previewSource(GridPoint point, Dragboard dragboard) {
    if (!pane.isOpen(point, new TileSize(1, 1), n -> false)) {
      // Dragged a source onto a tile, let the tile handle the drag and drop
      pane.setHighlight(false);
      return false;
    }
    SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);
    DataSource source = entry.get();
    Optional<String> componentName = Components.getDefault().pickComponentNameFor(source.getDataType());
    Optional<DataSource<?>> dummySource = DummySource.forTypes(source.getDataType());
    if (componentName.isPresent() && dummySource.isPresent()) {
      if (tilePreviewSize == null) {
        Components.getDefault().createComponent(componentName.get(), dummySource.get())
            .map(pane::sizeOfWidget)
            .ifPresent(size -> tilePreviewSize = size);
      }
      highlightPoint(point);
    }
    return true;
  }

  private boolean previewManyTiles(GridPoint point, Dragboard dragboard) {
    if (isLayoutTile(point)) {
      // Dragged onto a layout tile, let it handle the drag and drop
      highlights.forEach((t, h) -> pane.removeHighlight(h));
      highlights.clear();
      pane.setHighlight(false);
      return false;
    }
    DataFormats.MultipleTileData data = (DataFormats.MultipleTileData)
        dragboard.getContent(DataFormats.multipleTiles);
    int dx = point.col - data.getInitialPoint().col;
    int dy = point.row - data.getInitialPoint().row;
    boolean inBounds = data.getTileIds().stream()
        .map(id -> pane.tileMatching(tile -> tile.getId().equals(id)))
        .flatMap(TypeUtils.optionalStream())
        .map(pane::getTileLayout)
        .allMatch(layout -> layout.origin.col + dx >= 0 && layout.origin.row + dy >= 0);

    if (inBounds) {
      data.getTileIds().stream()
          .map(id -> pane.tileMatching(t -> t.getId().equals(id)))
          .flatMap(TypeUtils.optionalStream())
          .map(tile -> createHighlight(tile, dx, dy))
          .forEach(highlight -> {
            boolean open = pane.isOpen(highlight.getLocation(), highlight.getSize(), this::ignoreIfDragArtifact);
            highlight.pseudoClassStateChanged(PseudoClass.getPseudoClass("colliding"), !open);
          });
    } else {
      highlights.values().forEach(pane::removeHighlight);
      highlights.clear();
    }
    return true;
  }

  private WidgetPane.Highlight createHighlight(Tile<?> tile, int dx, int dy) {
    TileLayout layout = pane.getTileLayout(tile);
    int newCol = layout.origin.getCol() + dx;
    int newRow = layout.origin.getRow() + dy;
    WidgetPane.Highlight highlight = highlights.computeIfAbsent(tile, t -> pane.addHighlight());
    highlight.withLocation(new GridPoint(newCol, newRow));
    highlight.withSize(layout.size);
    highlights.put(tile, highlight);
    return highlight;
  }

  private boolean previewSingleTile(DragEvent event, GridPoint point, Dragboard dragboard) {
    if (isLayoutTile(point)) {
      if (((DataFormats.TileData) dragboard.getContent(DataFormats.singleTile)).getId()
          .equals(pane.tileAt(point)
              .map(Node::getId)
              .orElse(null))) {
        // Dragged a layout tile onto itself
        event.consume();
      } else {
        // Dragged a tile onto a layout, let the layout handle the drag and drop
        pane.setHighlight(false);
        return false;
      }
    }
    pane.setHighlight(true);
    pane.setHighlightPoint(point);
    DataFormats.TileData data = (DataFormats.TileData) dragboard.getContent(DataFormats.singleTile);
    pane.tileMatching(tile -> tile.getId().equals(data.getId()))
        .ifPresent(tile -> previewTile(tile, point.subtract(data.getLocalDragPoint())));
    return true;
  }

  private void highlightPoint(GridPoint point) {
    if (tilePreviewSize == null) {
      pane.setHighlight(false);
    } else {
      pane.setHighlight(true);
      pane.setHighlightPoint(point);
      pane.setHighlightSize(tilePreviewSize);
    }
  }

  /**
   * Previews a tile at a specific point in the pane.
   *
   * @param tile  the tile to preview
   * @param point the point to preview the tile at
   */
  private void previewTile(Tile<?> tile, GridPoint point) {
    TileSize size = tile.getSize();
    pane.setHighlightPoint(point);
    pane.setHighlightSize(size);
  }

  /**
   * Handles a DRAG_DROPPED event on the widget pane.
   *
   * @param event the drag event
   */
  private void handleDragDropped(DragEvent event) {
    Dragboard dragboard = event.getDragboard();
    GridPoint point = pane.pointAt(event.getX(), event.getY());

    // Dropping a source from the sources tree
    if (dragboard.hasContent(DataFormats.source)) {
      SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);
      dropSource(entry.get(), point);
    }

    // Dropping a tile onto the pane after moving it around
    if (dragboard.hasContent(DataFormats.singleTile)) {
      dropSingleTile((DataFormats.TileData) dragboard.getContent(DataFormats.singleTile), point);
    }

    // Dropping multiple tiles after moving them around
    if (dragboard.hasContent(DataFormats.multipleTiles)) {
      dropManyTiles((DataFormats.MultipleTileData) dragboard.getContent(DataFormats.multipleTiles), point);
    }

    // Dropping a widget from the gallery
    if (dragboard.hasContent(DataFormats.widgetType)) {
      dropGalleryWidget((String) dragboard.getContent(DataFormats.widgetType), point);
    }

    // Dragging a component out of a layout
    if (dragboard.hasContent(DataFormats.tilelessComponent)) {
      dropTilelessComponent((TilelessComponentData) dragboard.getContent(DataFormats.tilelessComponent), point);
    }
    cleanupWidgetDrag();
    tilePreviewSize = null;
    event.consume();
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
    Components.getDefault().pickComponentNameFor(source.getDataType())
        .flatMap(name -> Components.getDefault().createComponent(name, source))
        .filter(widget -> pane.isOpen(point, pane.sizeOfWidget(widget), n -> widget.getView() == n))
        .map(pane::addComponentToTile)
        .ifPresent(tile -> pane.moveNode(tile, point));
  }

  /**
   * Drops a single tile onto the tile pane at the given point.
   *
   * @param data  the tile data for the tile being dropped
   * @param point the point in the pane where the tile is being dropped
   */
  private void dropSingleTile(DataFormats.TileData data, GridPoint point) {
    pane.tileMatching(tile -> tile.getId().equals(data.getId()))
        .ifPresent(tile -> moveTile(tile, point.subtract(data.getLocalDragPoint())));
  }

  private void dropManyTiles(DataFormats.MultipleTileData data, GridPoint point) {
    int dx = point.col - data.getInitialPoint().getCol();
    int dy = point.row - data.getInitialPoint().getRow();
    boolean allMovable = data.getTileIds().stream()
        .map(id -> pane.tileMatching(tile -> tile.getId().equals(id)))
        .flatMap(TypeUtils.optionalStream())
        .map(pane::getTileLayout)
        .allMatch(layout -> canMove(layout, dx, dy));
    if (allMovable) {
      pane.getTiles().stream()
          .filter(tile -> data.getTileIds().contains(tile.getId()))
          .forEach(tile -> pane.moveNode(tile, pane.getTileLayout(tile).origin.add(dx, dy)));
    }
    highlights.values().forEach(pane::removeHighlight);
    highlights.clear();
  }

  private boolean canMove(TileLayout layout, int dx, int dy) {
    GridPoint origin = layout.origin;
    TileSize size = layout.size;
    return origin.getCol() + dx >= 0
        && origin.getRow() + dy >= 0
        && pane.isOpen(origin.add(dx, dy), size, this::ignoreIfDragArtifact);
  }

  private void dropGalleryWidget(String componentType, GridPoint point) {
    Components.getDefault().createComponent(componentType).ifPresent(c -> {
      TileSize size = pane.sizeOfWidget(c);
      if (pane.isOpen(point, size, __ -> false)) {
        c.setTitle(componentType);
        Tile<?> tile = pane.addComponentToTile(c);
        moveTile(tile, point);
      }
    });
  }

  private void dropTilelessComponent(TilelessComponentData data, GridPoint point) {
    Optional<Component> component = Components.getDefault().getByUuid(data.getComponentId());
    Optional<Layout> parent = Components.getDefault().getByUuid(data.getParentId())
        .flatMap(TypeUtils.optionalCast(Layout.class));
    component.ifPresent(c -> {
      parent.ifPresent(l -> l.removeChild(c));
      pane.addComponent(c, point);
    });
  }

  /**
   * Drops a widget into this pane at the given point.
   *
   * @param tile  the tile for the widget to drop
   * @param point the point in the tile pane to drop the widget at
   */
  private void moveTile(Tile tile, GridPoint point) {
    TileSize size = tile.getSize();
    if (pane.isOpen(point, size, n -> n == tile)) {
      pane.moveNode(tile, point);
    }
  }

  private void cleanupWidgetDrag() {
    pane.setHighlight(false);
    highlights.forEach((t, h) -> pane.removeHighlight(h));
    highlights.clear();
  }

  private boolean ignoreIfDragArtifact(Node node) {
    return selector.getSelectedTiles().contains(node)
        || node instanceof WidgetPane.Highlight;
  }
}
