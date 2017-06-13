package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.widget.TileSize;
import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A pane that represents components as tiles in a grid.
 */
@DefaultProperty("children")
public class TilePane extends GridPane {

  private static final int DEFAULT_COL_COUNT = 1;
  private static final int DEFAULT_ROW_COUNT = 1;
  private static final double MIN_TILE_SIZE = 32; // pixels
  private static final double DEFAULT_TILE_SIZE = 128; // pixels

  private final ObjectProperty<Integer> numColumns =
      new SimpleObjectProperty<>(this, "numColumns", 0);
  private final ObjectProperty<Integer> numRows =
      new SimpleObjectProperty<>(this, "numRows", 0);
  private final DoubleProperty tileSize =
      new SimpleDoubleProperty(this, "tileSize", DEFAULT_TILE_SIZE);

  /**
   * Creates a tile pane with one row and one column.
   */
  public TilePane() {
    this(DEFAULT_COL_COUNT, DEFAULT_ROW_COUNT);
  }

  /**
   * Creates a tile pane with the given number of columns and rows.
   *
   * @param numColumns the number of columns in the grid. Must be >= 1
   * @param numRows    the number of rows in the grid. Must be >= 1
   */
  public TilePane(int numColumns, int numRows) {
    this.numColumns.addListener((obs, oldCount, newCount) -> {
      if (newCount > oldCount) {
        IntStream.range(oldCount, newCount)
                 .mapToObj(__ -> createColumnConstraint())
                 .forEach(getColumnConstraints()::add);
      } else {
        getColumnConstraints().remove(newCount, oldCount);
      }
    });

    this.numRows.addListener((obs, oldCount, newCount) -> {
      if (newCount > oldCount) {
        IntStream.range(oldCount, newCount)
                 .mapToObj(__ -> createRowConstraint())
                 .forEach(getRowConstraints()::add);
      } else {
        getRowConstraints().remove(newCount, oldCount);
      }
    });

    setNumColumns(numColumns);
    setNumRows(numRows);
  }

  private ColumnConstraints createColumnConstraint() {
    ColumnConstraints constraints = new ColumnConstraints(
        getTileSize(), getTileSize(), getTileSize(), Priority.NEVER, HPos.LEFT, true);
    constraints.minWidthProperty().bind(tileSize);
    constraints.prefWidthProperty().bind(tileSize);
    constraints.maxWidthProperty().bind(tileSize);
    return constraints;
  }

  private RowConstraints createRowConstraint() {
    RowConstraints constraints = new RowConstraints(
        getTileSize(), getTileSize(), getTileSize(), Priority.NEVER, VPos.CENTER, true);
    constraints.minHeightProperty().bind(tileSize);
    constraints.prefHeightProperty().bind(tileSize);
    constraints.maxHeightProperty().bind(tileSize);
    return constraints;
  }

  public final Property<Integer> numColumnsProperty() {
    return numColumns;
  }

  /**
   * Gets the number of columns in the grid.
   */
  public final int getNumColumns() {
    return numColumns.get();
  }

  /**
   * Sets the number of columns in the grid.
   */
  public final void setNumColumns(int numColumns) {
    checkArgument(numColumns > 0, "There must be at least one column");
    this.numColumns.set(numColumns);
  }

  public final Property<Integer> numRowsProperty() {
    return numRows;
  }

  /**
   * Gets the number of rows in the grid.
   */
  public final int getNumRows() {
    return numRows.get();
  }

  /**
   * Sets the number of rows in the grid. This must be a positive number.
   */
  public final void setNumRows(int numRows) {
    checkArgument(numRows > 0, "There must be at least one row");
    this.numRows.set(numRows);
  }

  public final DoubleProperty tileSizeProperty() {
    return tileSize;
  }

  /**
   * Gets the size of the tiles in the grid.
   */
  public final double getTileSize() {
    return tileSize.get();
  }

  /**
   * Sets the size of the tiles in the grid.
   */
  public final void setTileSize(double tileSize) {
    checkArgument(tileSize > MIN_TILE_SIZE,
                  "Tile size must be at least " + MIN_TILE_SIZE + ", but was " + tileSize);
    this.tileSize.set(tileSize);
  }

  /**
   * Gets a grid point as close as possible to the given (x, y) coordinate in this grid's local
   * coordinate space.
   */
  public GridPoint pointAt(double x, double y) {
    double colCount = x / (getTileSize() + getHgap());
    double rowCount = y / (getTileSize() + getVgap());
    return new GridPoint((int) colCount, (int) rowCount);
  }

  /**
   * Sets the location of the given node in this tile pane.
   *
   * @param node  the node to set the location of
   * @param point the new location of the node
   * @throws IllegalArgumentException if the node is not a child of this pane
   */
  public void moveNode(Node node, GridPoint point) {
    if (!getChildren().contains(node)) {
      throw new IllegalArgumentException("The node is not a child of this pane: " + node);
    }
    setColumnIndex(node, point.col);
    setRowIndex(node, point.row);
  }

  /**
   * Gets the tile size closest to the given width and height in pixels.
   */
  public TileSize round(double width, double height) {
    return new TileSize(roundWidthToNearestTile(width), roundHeightToNearestTile(height));
  }

  /**
   * Rounds a tile's width in pixels to the nearest tile size.
   */
  public int roundWidthToNearestTile(double width) {
    // w = (n * tile_size) + ((n - 1) * vgap)
    //   = (n * tile_size) + (n * vgap) - vgap
    // w + vgap = (n * tile_size) + (n * vgap)
    //          = n * (tile_size + vgap)
    // n = (w + vgap) / (tile_size + vgap) QED
    // round n to nearest integer
    return (int) Math.max(1, Math.round((width + getVgap()) / (getTileSize() + getVgap())));
  }

  /**
   * Rounds a tile's height in pixels to the nearest tile size.
   */
  public int roundHeightToNearestTile(double height) {
    return (int) Math.max(1, Math.round((height + getHgap()) / (getTileSize() + getVgap())));
  }

  /**
   * Converts a {@link TileSize#getWidth() tile's width} to a pixel size.
   */
  public double tileSizeToWidth(int tileWidth) {
    checkArgument(tileWidth >= 1,
                  "The tile size must be a positive integer (was " + tileWidth + ")");
    return tileWidth * getTileSize() + (tileWidth - 1) * getHgap();
  }

  /**
   * Converts a {@link TileSize#getHeight() tile's height} to a pixel size.
   */
  public double tileSizeToHeight(int tileHeight) {
    checkArgument(tileHeight >= 1,
                  "The tile size must be a positive integer (was " + tileHeight + ")");
    return tileHeight * getTileSize() + (tileHeight - 1) * getVgap();
  }

  /**
   * Sets the size of the given node in this tile pane.
   *
   * @param node the node to resize
   * @param size the new size of the node
   */
  public void setSize(Node node, TileSize size) {
    if (!getChildren().contains(node)) {
      throw new IllegalArgumentException("The node is not a child of this pane: " + node);
    }
    setColumnSpan(node, size.getWidth());
    setRowSpan(node, size.getHeight());
  }

  public Node addTile(Node node, TileSize size) {
    return addTile(node, size.getWidth(), size.getHeight());
  }

  /**
   * Adds a node in the first available spot. The node will be wrapped in a pane to make it
   * easier to add single controls (buttons, labels, etc). This will fail (return {@code null}) iff:
   * <ul>
   * <li>{@code node} is {@code null}; or</li>
   * <li>{@code width} is zero or negative; or</li>
   * <li>{@code height} is zero or negative; or</li>
   * <li>there is no available space for a tile with the given dimensions</li>
   * </ul>
   *
   * @param node   the node to add
   * @param width  the width of the tile for the node. Must be >= 1
   * @param height the height of the tile for the node. Must be >= 1
   * @return the node added to the view
   */
  public Node addTile(Node node, int width, int height) {
    if (node == null) {
      // Can't add a null tile
      return null;
    }
    if (width < 1 || height < 1) {
      // Illegal dimensions
      return null;
    }
    GridPoint placement = firstPoint(width, height);
    if (placement == null) {
      // Nowhere to place the node
      return null;
    }

    add(node, placement.col, placement.row, width, height);
    setHalignment(node, HPos.LEFT);
    setValignment(node, VPos.TOP);
    return node;
  }

  /**
   * Finds the first point where a tile with the given dimensions can be added,
   * or {@code null} if no such point exists.
   *
   * @param width  the width of the tile trying to be added
   * @param height the height of the tile trying to be added
   */
  public GridPoint firstPoint(int width, int height) {
    // outer row, inner col to add tiles left-to-right in the upper rows
    // outer col, inner row would add tiles top-to-bottom from the left-hand columns (not intuitive)
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumColumns(); col++) {
        if (isOpen(col, row, width, height, n -> false)) {
          return new GridPoint(col, row);
        }
      }
    }
    return null;
  }

  /**
   * Checks if a tile with the given size can be added at the given point,
   * ignoring some nodes when calculating collisions.
   *
   * @param point    the point to check
   * @param tileSize the size of the tile
   * @param ignore   the nodes to ignore when determining collisions
   */
  public boolean isOpen(GridPoint point, TileSize tileSize, Predicate<Node> ignore) {
    return isOpen(point.getCol(), point.getRow(),
                  tileSize.getWidth(), tileSize.getHeight(),
                  ignore);
  }

  /**
   * Checks if a tile with the given width and height can be added at the point {@code (col, row)},
   * ignoring some nodes when calculating collisions.
   *
   * @param col        the column index of the point to check
   * @param row        the row index of the point to check
   * @param tileWidth  the width of the tile
   * @param tileHeight the height of the tile
   * @param ignore     the nodes to ignore when determining collisions
   */
  public boolean isOpen(int col, int row, int tileWidth, int tileHeight, Predicate<Node> ignore) {
    if (col + tileWidth > getNumColumns() || row + tileHeight > getNumRows()) {
      return false;
    }

    int x;
    int y;
    int width;
    int height;

    for (Node tile : getChildren()) {
      if (ignore.test(tile)) {
        continue;
      }
      try {
        x = GridPane.getColumnIndex(tile);
        y = GridPane.getRowIndex(tile);
        width = GridPane.getColumnSpan(tile);
        height = GridPane.getRowSpan(tile);
      } catch (NullPointerException e) {
        // Not a real child (Geppetto pls)
        continue;
      }

      if (x + width > col && y + height > row
          && x < col + tileWidth && y < row + tileHeight) {
        // Check intersection
        return false;
      }

    }

    return true;
  }

}
