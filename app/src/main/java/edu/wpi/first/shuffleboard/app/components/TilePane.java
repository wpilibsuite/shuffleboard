package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.RoundingMode;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import java.util.function.Predicate;
import java.util.stream.IntStream;

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
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A pane that represents components as tiles in a grid.
 */
@SuppressWarnings("PMD.GodClass") // This is not even close to being a god class
@DefaultProperty("children")
public class TilePane extends GridPane {

  private static final int DEFAULT_COL_COUNT = 1;
  private static final int DEFAULT_ROW_COUNT = 1;
  private static final double MIN_TILE_SIZE = 32; // pixels

  private final ObjectProperty<Integer> numColumns =
      new SimpleObjectProperty<>(this, "numColumns", 0);
  private final ObjectProperty<Integer> numRows =
      new SimpleObjectProperty<>(this, "numRows", 0);
  private final DoubleProperty tileSize =
      new SimpleDoubleProperty(this, "tileSize", AppPreferences.getInstance().getDefaultTileSize());

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

    // Make sure the tile size is always at least the minimum allowable
    tileSize.addListener((__, oldSize, newSize) -> {
      if (newSize.doubleValue() < MIN_TILE_SIZE) {
        if (oldSize.doubleValue() < MIN_TILE_SIZE) {
          setTileSize(MIN_TILE_SIZE);
        } else {
          setTileSize(oldSize.doubleValue());
        }
      }
    });
  }

  private ColumnConstraints createColumnConstraint() {
    ColumnConstraints constraints = new ColumnConstraints(
        MIN_TILE_SIZE, getTileSize(), Region.USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true);
    constraints.minWidthProperty().bind(tileSize);
    constraints.prefWidthProperty().bind(tileSize);
    constraints.maxWidthProperty().bind(tileSize);
    return constraints;
  }

  private RowConstraints createRowConstraint() {
    RowConstraints constraints = new RowConstraints(
        MIN_TILE_SIZE, getTileSize(), Region.USE_PREF_SIZE, Priority.NEVER, VPos.CENTER, true);
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
    checkArgument(tileSize >= MIN_TILE_SIZE,
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
   *
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
    return roundWidthToNearestTile(width, RoundingMode.NEAREST);
  }

  /**
   * Rounds a tile's width in pixels to the nearest tile size.
   *
   * @param width        the width to round
   * @param roundingMode the mode to use to round the fractional tile size
   */
  public int roundWidthToNearestTile(double width, RoundingMode roundingMode) {
    // w = (n * tile_size) + ((n - 1) * vgap)
    //   = (n * tile_size) + (n * vgap) - vgap
    // w + vgap = (n * tile_size) + (n * vgap)
    //          = n * (tile_size + vgap)
    // n = (w + vgap) / (tile_size + vgap) QED
    // round n to nearest integer
    return Math.max(1, roundingMode.round((width + getHgap()) / (getTileSize() + getHgap())));
  }

  /**
   * Rounds a tile's height in pixels to the nearest tile size.
   */
  public int roundHeightToNearestTile(double height) {
    return roundHeightToNearestTile(height, RoundingMode.NEAREST);
  }

  /**
   * Rounds a tile's height in pixels to the nearest tile size.
   *
   * @param height       the height to round
   * @param roundingMode the mode to use to round the fractional tile size
   */
  public int roundHeightToNearestTile(double height, RoundingMode roundingMode) {
    return Math.max(1, roundingMode.round((height + getVgap()) / (getTileSize() + getVgap())));
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
   * @param node the node to add
   * @param size the size of the node to add
   *
   * @return the node added to the view
   */
  public Node addTile(Node node, TileSize size) {
    GridPoint placement = firstPoint(size);
    if (placement == null) {
      // Nowhere to place the node
      return null;
    }
    return addTile(node, placement, size);
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
   * @param node     the node to add
   * @param location the location to add the node
   * @param size     the size of the node to add
   *
   * @return the node added to the view
   */
  public Node addTile(Node node, GridPoint location, TileSize size) {
    if (node == null) {
      // Can't add a null tile
      return null;
    }

    add(node, location.col, location.row, size.getWidth(), size.getHeight());
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
   * Finds the first point where a tile with the given size can be added, or {@code null} if no such point exists.
   *
   * @param tileSize the tile size to check
   */
  public GridPoint firstPoint(TileSize tileSize) {
    return firstPoint(tileSize.getWidth(), tileSize.getHeight());
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
    if (col < 0 || col + tileWidth > getNumColumns()
        || row < 0 || row + tileHeight > getNumRows()) {
      return false;
    }
    return !isOverlapping(col, row, tileWidth, tileHeight, ignore);
  }

  /**
   * Identical to {@link #isOverlapping(int, int, int, int, Predicate)} but takes a tile layout that contains the
   * row, column, width, and height data.
   *
   * @see #isOverlapping(int, int, int, int, Predicate)
   */
  public boolean isOverlapping(TileLayout layout, Predicate<Node> ignore) {
    return isOverlapping(layout.origin.col, layout.origin.row, layout.size.getWidth(), layout.size.getHeight(), ignore);
  }

  /**
   * Checks if a tile with the given width and height would overlap a pre-existing tile at the point {@code (col, row)},
   * ignoring some nodes when calculating collisions. Note that this method does <i>not</i> perform bounds checking;
   * use {@link #isOpen(int, int, int, int, Predicate) isOpen} to check if a widget can be placed at that point.
   *
   * @param col        the column index of the point to check
   * @param row        the row index of the point to check
   * @param tileWidth  the width of the tile
   * @param tileHeight the height of the tile
   * @param ignore     a predicate to use to ignore nodes when calculating collisions
   */
  public boolean isOverlapping(int col, int row, int tileWidth, int tileHeight, Predicate<Node> ignore) {
    for (Node child : getChildren()) {
      if (ignore.test(child)) {
        continue;
      }
      // All "real" children have a column index, row index, and column and row spans
      // Other children (like the grid lines) don't have these properties and will throw null pointers
      // when trying to access these properties
      if (GridPane.getColumnIndex(child) != null) {
        int x = GridPane.getColumnIndex(child);
        int y = GridPane.getRowIndex(child);
        int width = GridPane.getColumnSpan(child);
        int height = GridPane.getRowSpan(child);
        if (x + width > col && y + height > row
            && x - tileWidth < col && y - tileHeight < row) {
          // There's an intersection
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the layout of a tile in this tile pane.
   */
  public TileLayout getTileLayout(Tile<?> tile) {
    return new TileLayout(GridPoint.fromNode(tile), tile.getSize());
  }

}
