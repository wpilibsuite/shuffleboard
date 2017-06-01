package edu.wpi.first.shuffleboard.dnd;

import edu.wpi.first.shuffleboard.WidgetTile;
import edu.wpi.first.shuffleboard.components.TilePane;
import edu.wpi.first.shuffleboard.widget.TileSize;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 * {@link TileDragResizer} can be used to add mouse listeners to a {@link WidgetTile} and make it
 * resizable by the user by clicking and dragging the border in the same way as a window.
 */
public class TileDragResizer {

  /**
   * The margin around the control that a user can click in to start resizing the tile.
   */
  private static final int RESIZE_MARGIN = 10;

  private static final double TILE_SIZE = 128;

  private final TilePane tilePane;
  private final WidgetTile tile;

  private double x;
  private double y;

  private boolean initMinHeight;
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
    public final boolean vertical;
    /**
     * Whether or not this location allows a tile to be resized horizontally.
     */
    public final boolean horizontal;

    ResizeLocation(Cursor cursor, boolean vertical, boolean horizontal) {
      this.cursor = cursor;
      this.vertical = vertical;
      this.horizontal = horizontal;
    }
  }

  private TileDragResizer(TilePane tilePane, WidgetTile tile) {
    this.tilePane = tilePane;
    this.tile = tile;
  }

  /**
   * Makes the given tile resizable.
   *
   * @param tilePane the pane containing the tile to make resizable
   * @param tile     the tile to make resizable
   */
  public static TileDragResizer makeResizable(TilePane tilePane, WidgetTile tile) {
    final TileDragResizer resizer = new TileDragResizer(tilePane, tile);

    tile.addEventHandler(MouseEvent.MOUSE_PRESSED, resizer::mousePressed);
    tile.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizer::mouseDragged);
    tile.addEventHandler(MouseEvent.MOUSE_MOVED, resizer::mouseOver);
    tile.addEventHandler(MouseEvent.MOUSE_RELEASED, resizer::mouseReleased);

    return resizer;
  }

  private void mouseReleased(MouseEvent event) {
    dragging = false;
    tile.setCursor(Cursor.DEFAULT);
    resizeLocation = ResizeLocation.NONE;

    // round size to nearest tile size
    final int tileWidth = tilePane.roundWidthToNearestTile(tile.getMinWidth());
    final int tileHeight = tilePane.roundHeightToNearestTile(tile.getMinHeight());
    tile.setMinWidth(tilePane.tileSizeToWidth(tileWidth));
    tile.setMinHeight(tilePane.tileSizeToHeight(tileHeight));
    tile.setSize(new TileSize(tileWidth, tileHeight));
    GridPane.setColumnSpan(tile, tileWidth);
    GridPane.setRowSpan(tile, tileHeight);
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

    double mouseX = event.getX();
    double mouseY = event.getY();

    double newWidth = tile.getMinWidth() + (mouseX - x);
    double newHeight = tile.getMinHeight() + (mouseY - y);

    if (resizeLocation.horizontal && newWidth >= TILE_SIZE) {
      tile.setMinWidth(newWidth);
      tile.setMaxWidth(newWidth);
      GridPane.setColumnIndex(tile, GridPane.getColumnIndex(tile));
    }
    if (resizeLocation.vertical && newHeight >= TILE_SIZE) {
      tile.setMinHeight(newHeight);
      tile.setMaxHeight(newHeight);
      GridPane.setRowIndex(tile, GridPane.getRowIndex(tile));
    }

    x = mouseX;
    y = mouseY;
  }

  private void mousePressed(MouseEvent event) {
    // ignore clicks outside of the draggable margin
    if (!isInDraggableZone(event)) {
      return;
    }

    dragging = true;

    // make sure that the minimum height is set to the current height once,
    // setting a min height that is smaller than the current height will
    // have no effect
    if (!initMinHeight) {
      tile.setMinHeight(tile.getHeight());
      tile.setMinWidth(tile.getWidth());
      initMinHeight = true;
    }

    x = event.getX();
    y = event.getY();
  }

  public boolean isDragging() {
    return dragging;
  }

}
