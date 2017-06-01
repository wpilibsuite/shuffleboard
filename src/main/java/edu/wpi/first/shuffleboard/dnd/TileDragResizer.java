package edu.wpi.first.shuffleboard.dnd;

import edu.wpi.first.shuffleboard.WidgetTile;
import edu.wpi.first.shuffleboard.components.TilePane;
import edu.wpi.first.shuffleboard.widget.TileSize;
import javafx.scene.Cursor;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * {@link TileDragResizer} can be used to add mouse listeners to a {@link WidgetTile} and make it
 * resizable by the user by clicking and dragging the border in the same way as a window.
 */
public final class TileDragResizer {

  /**
   * Keep track of resizers to avoid creating more than one for the same tile.
   */
  private static final Map<WidgetTile, TileDragResizer> resizers = new WeakHashMap<>();

  /**
   * The margin around the control that a user can click in to start resizing the tile.
   */
  private static final int RESIZE_MARGIN = 10;

  private final TilePane tilePane;
  private final WidgetTile tile;

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

  private TileDragResizer(TilePane tilePane, WidgetTile tile) {
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
  public static TileDragResizer makeResizable(TilePane tilePane, WidgetTile tile) {
    return resizers.computeIfAbsent(tile, __ -> new TileDragResizer(tilePane, tile));
  }

  private void reset() {
    didDragInit = false;
    dragging = false;
    resizeLocation = ResizeLocation.NONE;
    lastX = 0;
    lastY = 0;
  }

  private void mouseReleased(MouseEvent event) {
    dragging = false;
    tile.setCursor(Cursor.DEFAULT);
    resizeLocation = ResizeLocation.NONE;

    // round size to nearest tile size
    final int tileWidth = tilePane.roundWidthToNearestTile(tile.getMinWidth());
    final int tileHeight = tilePane.roundHeightToNearestTile(tile.getMinHeight());

    // limit size to prevent exceeding the bounds of the grid
    final int boundedWidth = Math.min(tilePane.getNumColumns() - GridPane.getColumnIndex(tile),
                                      tileWidth);
    final int boundedHeight = Math.min(tilePane.getNumRows() - GridPane.getRowIndex(tile),
                                       tileHeight);

    tile.setMinWidth(tilePane.tileSizeToWidth(boundedWidth));
    tile.setMinHeight(tilePane.tileSizeToHeight(boundedHeight));
    tile.setSize(new TileSize(boundedWidth, boundedHeight));
    GridPane.setColumnSpan(tile, boundedWidth);
    GridPane.setRowSpan(tile, boundedHeight);
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
    if (inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseX)) {
      // left side
      if (inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseY)) {
        // top
        return ResizeLocation.NORTH_WEST;
      } else if (inRange(h - RESIZE_MARGIN, h + RESIZE_MARGIN, mouseY)) {
        // bottom
        return ResizeLocation.SOUTH_WEST;
      } else {
        // middle
        return ResizeLocation.WEST;
      }
    } else if (inRange(w - RESIZE_MARGIN, w + RESIZE_MARGIN, mouseX)) {
      // right side
      if (inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseY)) {
        // top
        return ResizeLocation.NORTH_EAST;
      } else if (inRange(h - RESIZE_MARGIN, h + RESIZE_MARGIN, mouseY)) {
        // bottom
        return ResizeLocation.SOUTH_EAST;
      } else {
        // middle
        return ResizeLocation.EAST;
      }
    } else if (inRange(-RESIZE_MARGIN, RESIZE_MARGIN, mouseY)) {
      // top
      return ResizeLocation.NORTH;
    } else if (inRange(h - RESIZE_MARGIN, h + RESIZE_MARGIN, mouseY)) {
      // bottom
      return ResizeLocation.SOUTH;
    } else {
      // not close enough to an edge
      return ResizeLocation.NONE;
    }
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
      tile.setMinWidth(newWidth);
      tile.setMaxWidth(newWidth);
    }
    if (resizeLocation.isVertical && newHeight >= tilePane.getTileSize()) {
      tile.setMinHeight(newHeight);
      tile.setMaxHeight(newHeight);
    }

    lastX = mouseX;
    lastY = mouseY;
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
  }

  public boolean isDragging() {
    return dragging;
  }

}
