package edu.wpi.first.shuffleboard.elements;

import edu.wpi.first.shuffleboard.Point;
import edu.wpi.first.shuffleboard.widget.Size;
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
import javafx.scene.layout.StackPane;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A pane that represents components as tiles in a grid.
 */
@DefaultProperty("children")
public class TilePane extends GridPane {

  private static final int DEFAULT_COL_COUNT = 0;
  private static final int DEFAULT_ROW_COUNT = 0;
  private static final double MIN_TILE_SIZE = 32;
  private static final double DEFAULT_TILE_SIZE = 128; // pixels

  private final ObjectProperty<Integer> cols =
      new SimpleObjectProperty<>(this, "cols", DEFAULT_COL_COUNT);
  private final ObjectProperty<Integer> rows =
      new SimpleObjectProperty<>(this, "rows", DEFAULT_ROW_COUNT);
  private final DoubleProperty tileSize =
      new SimpleDoubleProperty(this, "tileSize", DEFAULT_TILE_SIZE);

  /**
   * Creates a tile pane with no rows or columns.
   */
  public TilePane() {
    this(DEFAULT_COL_COUNT, DEFAULT_ROW_COUNT);
  }

  /**
   * Creates a tile pane with the given number of columns and rows.
   *
   * @param cols the number of columns in the grid. Must be >= 1
   * @param rows the number of rows in the grid. Must be >= 1
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public TilePane(int cols, int rows) {
    this.cols.addListener((obs, o, n) -> {
      if (n > o) {
        for (int i = 0; i < n - o; i++) {
          ColumnConstraints c = new ColumnConstraints(
              MIN_TILE_SIZE, getTileSize(), 1e3, Priority.ALWAYS, HPos.LEFT, true);
          c.prefWidthProperty().bind(tileSize);
          getColumnConstraints().add(c);
        }
      } else {
        getColumnConstraints().remove(n, o);
      }
    });

    this.rows.addListener((obs, o, n) -> {
      if (n > o) {
        for (int i = 0; i < n - o; i++) {
          RowConstraints c = new RowConstraints(
              MIN_TILE_SIZE, getTileSize(), 1e4, Priority.ALWAYS, VPos.CENTER, true);
          c.prefHeightProperty().bind(tileSize);
          getRowConstraints().add(c);
        }
      } else {
        getRowConstraints().remove(n, o);
      }
    });

    if (cols > 0) {
      setCols(cols);
    }
    if (rows > 0) {
      setRows(rows);
    }
  }

  public final Property<Integer> colsProperty() {
    return cols;
  }

  /**
   * Gets the number of columns in the grid.
   */
  public final int getCols() {
    return cols.get();
  }

  /**
   * Sets the number of columns in the grid.
   */
  public final void setCols(int cols) {
    checkArgument(cols > 0, "There must be at least one column");
    this.cols.set(cols);
  }

  public final Property<Integer> rowsProperty() {
    return rows;
  }

  /**
   * Gets the number of rows in the grid.
   */
  public final int getRows() {
    return rows.get();
  }

  /**
   * Sets the number of rows in the grid. This must be a positive number.
   */
  public final void setRows(int rows) {
    checkArgument(rows > 0, "There must be at least one row");
    this.rows.set(rows);
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

  public Node addTile(Node node, Size size) {
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
    Point placement = firstPoint(width, height);
    if (placement == null) {
      // Nowhere to place the node
      return null;
    }

    StackPane wrapper = new StackPane(node);
    wrapper.getStyleClass().add("tile");

    add(wrapper, placement.col, placement.row, width, height);
    return wrapper;
  }

  /**
   * Finds the first point where a tile with the given dimensions can be added,
   * or {@code null} if no such point exists.
   *
   * @param width  the width of the tile trying to be added
   * @param height the height of the tile trying to be added
   */
  public Point firstPoint(int width, int height) {
    // outer row, inner col to add tiles left-to-right in the upper rows
    // outer col, inner row would add tiles top-to-bottom from the left-hand columns (not intuitive)
    for (int row = 0; row < getRows(); row++) {
      for (int col = 0; col < getCols(); col++) {
        if (isOpen(col, row, width, height)) {
          return new Point(col, row);
        }
      }
    }
    return null;
  }

  /**
   * Checks if a tile with the given width and height can be added at the point {@code (col, row)}.
   *
   * @param col        the column index of the point to check
   * @param row        the row index of the point to check
   * @param tileWidth  the width of the tile
   * @param tileHeight the height of the tile
   */
  public boolean isOpen(int col, int row, int tileWidth, int tileHeight) {
    if (col + tileWidth > getCols() || row + tileHeight > getRows()) {
      return false;
    }

    int x;
    int y;
    int width;
    int height;

    for (Node tile : getChildren()) {
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
