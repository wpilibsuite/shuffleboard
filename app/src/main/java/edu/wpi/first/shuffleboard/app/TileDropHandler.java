package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.dnd.DragUtils;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.app.components.LayoutTile;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;

import java.util.Optional;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

/**
 * Helper class for handling dropping various types of data onto a tile.
 */
final class TileDropHandler implements EventHandler<DragEvent> {

  private final WidgetPane pane;
  private final Tile<?> tile;

  TileDropHandler(WidgetPane pane, Tile<?> tile) {
    this.pane = pane;
    this.tile = tile;
  }

  @Override
  public void handle(DragEvent event) {
    final Dragboard dragboard = event.getDragboard();
    final Point2D eventPos = screenPos(event); // NOPMD

    // Dragging a source onto a tile
    if (dragboard.hasContent(DataFormats.source) && tile.getContent() instanceof Sourced) {
      SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);
      dropSourceOnTile(entry);
      event.consume();

      return;
    }

    // Moving a layout tile around
    if (dragboard.hasContent(DataFormats.singleTile) && tile instanceof LayoutTile) {
      DataFormats.TileData data = (DataFormats.TileData) event.getDragboard().getContent(DataFormats.singleTile);

      if (tile.getId().equals(data.getId())) {
        return;
      }
      dropSingleTileOntoLayout(data, eventPos);
      event.consume();

      return;
    }

    // Dropping multiple tiles onto a layout
    if (dragboard.hasContent(DataFormats.multipleTiles) && tile instanceof LayoutTile) {
      DataFormats.MultipleTileData data =
          (DataFormats.MultipleTileData) event.getDragboard().getContent(DataFormats.multipleTiles);
      if (data.getTileIds().contains(tile.getId())) {
        return;
      }
      dropTilesOntoLayout(data, eventPos);
      event.consume();
    }

    // Dragging a widget from the gallery
    if (dragboard.hasContent(DataFormats.widgetType) && tile instanceof LayoutTile) {
      String widgetType = (String) dragboard.getContent(DataFormats.widgetType);

      dropGalleryWidgetOntoLayout(widgetType, eventPos);
      event.consume();

      return;
    }

    // Dragging a source from the sources tree
    if (dragboard.hasContent(DataFormats.source) && tile instanceof LayoutTile) {
      SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);

      dropSourceOntoLayout((Layout) tile.getContent(), entry, eventPos);
      event.consume();

      return;
    }

    // Dragging a component out of a layout
    if (dragboard.hasContent(DataFormats.tilelessComponent)) {
      DataFormats.TilelessComponentData data = DragUtils.getData(dragboard, DataFormats.tilelessComponent);
      dropComponentOutOfLayout(data, eventPos);
      event.consume();

      return;
    }
  }

  /**
   * Drops a component that was dragged out of a layout onto the tile, if it contains a layout.
   *
   * @param data      the data of the component being dragged
   * @param screenPos the screen coordinates where the component was dropped
   */
  private void dropComponentOutOfLayout(DataFormats.TilelessComponentData data, Point2D screenPos) {
    Optional<Component> component = Components.getDefault().getByUuid(data.getComponentId());
    Optional<Layout> parent = Components.getDefault().getByUuid(data.getParentId())
        .flatMap(TypeUtils.optionalCast(Layout.class));
    component.ifPresent(c -> {
      if (tile.getContent() instanceof Layout) {
        parent.ifPresent(l -> l.removeChild(c));
        add((Layout) tile.getContent(), c, screenPos);
      } else {
        ((Tile) tile).setContent(c);
      }
    });
  }

  /**
   * Drops a data source onto the tile, if it contains a layout.
   *
   * @param layout    the layout contained in the tile
   * @param entry     the source entry being dragged
   * @param screenPos the screen coordinates where the source was dropped
   */
  private void dropSourceOntoLayout(Layout layout, SourceEntry entry, Point2D screenPos) {
    DataSource<?> source = entry.get();
    Components.getDefault().pickComponentNameFor(entry.get().getDataType())
        .flatMap(name -> Components.getDefault().createWidget(name, source))
        .ifPresent(w -> add(layout, w, screenPos));
  }

  /**
   * Drops a widget from the gallery onto the tile, if it contains a layout.
   *
   * @param widgetType the type of the widget that is being dragged
   * @param screenPos  the screen coordinates where the widget was dropped
   */
  private void dropGalleryWidgetOntoLayout(String widgetType, Point2D screenPos) {
    Components.getDefault().createWidget(widgetType).ifPresent(widget -> {
      add((Layout) tile.getContent(), widget, screenPos);
    });
  }

  /**
   * Drops multiple tiles onto the tile, if it contains a layout.
   *
   * @param data      the data for the tiles being dropped
   * @param screenPos the screen coordinates where the tiles were dropped
   */
  private void dropTilesOntoLayout(DataFormats.MultipleTileData data, Point2D screenPos) {
    data.getTileIds().stream()
        .map(id -> pane.tileMatching(t -> t.getId().equals(id)))
        .flatMap(TypeUtils.optionalStream())
        .forEach(t -> {
          Component content = pane.removeTile(t);
          add((Layout) tile.getContent(), content, screenPos);
        });
  }

  /**
   * Drops a data source onto the tile, if it does <i>not</i> contain a layout.
   *
   * @param entry the source being dragged
   */
  private void dropSourceOnTile(SourceEntry entry) {
    DataSource source = entry.get();
    Sourced sourced = (Sourced) tile.getContent();
    sourced.addSource(source);
  }

  /**
   * Drops a single tile onto the tile, if it contains a layout.
   *
   * @param data      the data for the tile being dragged
   * @param screenPos the screen coordinates where the tile was dropped
   */
  private void dropSingleTileOntoLayout(DataFormats.TileData data, Point2D screenPos) {
    pane.tileMatching(t -> t.getId().equals(data.getId()))
        .ifPresent(t -> {
          Component content = pane.removeTile(t);
          add((Layout) tile.getContent(), content, screenPos);
        });
  }

  /**
   * Stores the screen position of a drag event in a Point2D object for easy encapsulation.
   *
   * @param event the drag event
   *
   * @return a point containing the screen coordinates of the drag event
   */
  private static Point2D screenPos(DragEvent event) {
    return new Point2D(event.getScreenX(), event.getScreenY());
  }

  /**
   * Adds a component to a layout at the given screen coordinates. The screen position is translated to layout-local
   * coordinates.
   *
   * @param layout    the layout to add to
   * @param component the component to add
   * @param screenPos the screen position of the drop
   */
  private static void add(Layout layout, Component component, Point2D screenPos) {
    Point2D point = layout.getView().screenToLocal(screenPos);
    layout.addChild(component, point.getX(), point.getY());
  }

}
