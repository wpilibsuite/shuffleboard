package edu.wpi.first.shuffleboard.app.dnd;

import edu.wpi.first.shuffleboard.api.util.RoundingMode;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.TileLayout;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.scene.Cursor;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * {@code TileDragResizer} can be used to add mouse listeners to a {@link WidgetTile} and make it
 * resizable by the user by clicking and dragging the border in the same way as a window.
 */
public final class TileDragResizer {

  /**
   * Keep track of resizers to avoid creating more than one for the same tile.
   */
  private static final Map<Tile, TileDragResizer> resizers = new WeakHashMap<>();

  /**
   * The margin around the control that a user can click in to start resizing the tile.
   */
  private static final int RESIZE_MARGIN = 10;

  private final WidgetPane tilePane;
  private final Tile tile;

  private double lastX;
  private double lastY;

  private boolean didDragInit;
  private boolean dragging;
  private ResizeLocation resizeLocation = ResizeLocation.NONE;

  private enum ResizeLocation {
    NONE(Cursor.DEFAULT, false, false),
    NORTH(Cursor.N_RESIZE, true, false),
    NORTH_EAST(Cursor.NE_RESIZE, true, true),
    EAST(Cursor.E_RESIZE, false, true),
    SOUTH_EAST(Cursor.SE_RESIZE, true, true),
    SOUTH(Cursor.S_RESIZE, true, false),
    SOUTH_WEST(Cursor.SW_RESIZE, true, true),
    WEST(Cursor.W_RESIZE, false, true),
    NORTH_WEST(Cursor.NW_RESIZE, true, true);

    /**
     * The cursor to use when resizing in this location.
     */
    public final Cursor cursor;
    /**
     * Whether or not this location allows a tile to be resized vertically.
     */
    public final boolean isVertical;
    /**
     * Whether or not this location allows a tile to be resized horizontally.
     */
    public final boolean isHorizontal;

    ResizeLocation(Cursor cursor, boolean isVertical, boolean isHorizontal) {
      this.cursor = cursor;
      this.isVertical = isVertical;
      this.isHorizontal = isHorizontal;
    }
  }

  private TileDragResizer(WidgetPane tilePane, Tile tile) {
    this.tilePane = tilePane;
    this.tile = tile;
    tile.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
    tile.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
    tile.addEventHandler(MouseEvent.MOUSE_MOVED, this::mouseOver);
    tile.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
    tile.addEventHandler(DragEvent.DRAG_DONE, __ -> reset());
  }

  /**
   * Makes the given tile resizable.
   *
   * @param tilePane the pane containing the tile to make resizable
   * @param tile     the tile to make resizable
   */
  public static TileDragResizer makeResizable(WidgetPane tilePane, Tile tile) {
    return resizers.computeIfAbsent(tile, __ -> new TileDragResizer(tilePane, tile));
  }

  private void reset() {
    didDragInit = false;
    dragging = false;
    resizeLocation = ResizeLocation.NONE;
    lastX = 0;
    lastY = 0;
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void mouseReleased(MouseEvent event) {
    if (!dragging) {
      return;
    }
    dragging = false;
    tile.setCursor(Cursor.DEFAULT);
    resizeLocation = ResizeLocation.NONE;

    TileSize size = finalSize();

    tile.setSize(size);
    GridPane.setColumnSpan(tile, size.getWidth());
    GridPane.setRowSpan(tile, size.getHeight());
    tilePane.setHighlight(false);
    ResizeUtils.setCurrentTile(null);
  }

  /**
   * Gets the final size of the tile if resizing were to be completed at the instant this method is called.
   */
  private TileSize finalSize() {
    // round size to nearest tile size
    final int tileWidth = tilePane.roundWidthToNearestTile(tile.getWidth());
    final int tileHeight = tilePane.roundHeightToNearestTile(tile.getHeight());

    // Make sure the tile never gets smaller than it's content minimum size, otherwise weird clipping occurs
    Pane view = tile.getContent().getView();
    int minWidth = tilePane.roundWidthToNearestTile(view.getMinWidth(), RoundingMode.UP);
    int minHeight = tilePane.roundHeightToNearestTile(view.getMinHeight(), RoundingMode.UP);

    // limit size to prevent exceeding the bounds of the grid
    int boundedWidth = Math.min(tilePane.getNumColumns() - GridPane.getColumnIndex(tile), tileWidth);
    int boundedHeight = Math.min(tilePane.getNumRows() - GridPane.getRowIndex(tile), tileHeight);

    // limit size to never be less than the minimum size of the content
    boundedWidth = Math.max(minWidth, boundedWidth);
    boundedHeight = Math.max(minHeight, boundedHeight);

    return new TileSize(boundedWidth, boundedHeight);
  }

  private void mouseOver(MouseEvent event) {
    if (isInDraggableZone(event) || dragging) {
      tile.setCursor(resizeLocation.cursor);
    } else {
      tile.setCursor(Cursor.DEFAULT);
    }
  }

  /**
   * Gets the most appropriate resize location for a mouse event.
   */
  private ResizeLocation getResizeLocation(MouseEvent event) {
    final double mouseX = event.getX();
    final double mouseY = event.getY();
    final double w = tile.getWidth();
    final double h = tile.getHeight();

    final boolean top = inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseY);
    final boolean left = inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseX);
    final boolean bottom = inRange(h - RESIZE_MARGIN, h + RESIZE_MARGIN, mouseY);
    final boolean right = inRange(w - RESIZE_MARGIN, w + RESIZE_MARGIN, mouseX);

    if (left) {
      if (top) {
        return ResizeLocation.NORTH_WEST;
      } else if (bottom) {
        return ResizeLocation.SOUTH_WEST;
      } else {
        return ResizeLocation.WEST;
      }
    } else {
      if (right) {
        if (top) {
          return ResizeLocation.NORTH_EAST;
        } else if (bottom) {
          return ResizeLocation.SOUTH_EAST;
        } else {
          return ResizeLocation.EAST;
        }
      } else if (top) {
        return ResizeLocation.NORTH;
      } else if (bottom) {
        return ResizeLocation.SOUTH;
      }
    }
    // not close enough to an edge
    return ResizeLocation.NONE;
  }

  private static boolean inRange(double min, double max, double check) {
    return check >= min && check <= max;
  }

  private boolean isInDraggableZone(MouseEvent event) {
    resizeLocation = getResizeLocation(event);
    return resizeLocation != ResizeLocation.NONE;
  }

  private void mouseDragged(MouseEvent event) {
    if (!dragging) {
      return;
    }

    final double mouseX = event.getX();
    final double mouseY = event.getY();

    final double newWidth = tile.getMinWidth() + (mouseX - lastX);
    final double newHeight = tile.getMinHeight() + (mouseY - lastY);

    if (resizeLocation.isHorizontal && newWidth >= tilePane.getTileSize()) {
      if (tile.getContent().getView().getMinWidth() < newWidth) {
        tile.setMinWidth(newWidth);
      }
      tile.setMaxWidth(newWidth);
    }
    if (resizeLocation.isVertical && newHeight >= tilePane.getTileSize()) {
      if (tile.getContent().getView().getMinHeight() < newHeight) {
        tile.setMinHeight(newHeight);
      }
      tile.setMaxHeight(newHeight);
    }

    lastX = mouseX;
    lastY = mouseY;
    TileLayout layout = tilePane.getTileLayout(tile);
    tilePane.setHighlight(true);
    tilePane.setHighlightPoint(layout.origin);
    tilePane.setHighlightSize(finalSize());
  }

  private void mousePressed(MouseEvent event) {
    // ignore clicks outside of the draggable margin
    if (!isInDraggableZone(event)) {
      return;
    }

    dragging = true;

    // make sure that the minimum size is set to the current size once;
    // setting a min size that is smaller than the current size will have no effect
    if (!didDragInit) {
      tile.setMinHeight(tile.getHeight());
      tile.setMinWidth(tile.getWidth());
      didDragInit = true;
    }

    lastX = event.getX();
    lastY = event.getY();
    ResizeUtils.setCurrentTile(tile);
    TileLayout layout = tilePane.getTileLayout(tile);
    tilePane.setHighlight(true);
    tilePane.setHighlightSize(layout.size);
    tilePane.setHighlightPoint(layout.origin);
  }

  public boolean isDragging() {
    return dragging;
  }

}
