package edu.wpi.first.shuffleboard.api.util;

import java.io.Serializable;
import java.util.Objects;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a point in a grid.
 */
public class GridPoint implements Serializable {

  public final int col;
  public final int row;

  /**
   * Creates a point at the given column and row indices.
   *
   * @throws IllegalArgumentException if either {@code col} or {@code row} is negative
   */
  public GridPoint(int col, int row) {
    checkArgument(col >= 0, "Column index must be non-negative, was " + col);
    checkArgument(row >= 0, "Row index must be non-negative, was " + row);
    this.col = col;
    this.row = row;
  }

  /**
   * Creates a new point from a node. The node must have both {@link GridPane#setColumnIndex GridPane.columnIndex}
   * and {@link GridPane#setRowIndex GridPane.rowIndex} set.
   *
   * @param node the node to get the grid point of
   *
   * @throws IllegalArgumentException if either columnIndex or rowIndex has not been set
   */
  public static GridPoint fromNode(Node node) {
    Integer columnIndex = GridPane.getColumnIndex(node);
    Integer rowIndex = GridPane.getRowIndex(node);
    if (columnIndex == null || rowIndex == null) {
      throw new IllegalArgumentException(
          "Invalid grid constraints: columnIndex=" + columnIndex + ", rowIndex=" + rowIndex);
    }
    return new GridPoint(columnIndex, rowIndex);
  }

  /**
   * Sets the column and row indices of a node to this point.
   *
   * @param node the node to set the position of
   */
  public void applyTo(Node node) {
    GridPane.setColumnIndex(node, col);
    GridPane.setRowIndex(node, row);
  }

  /**
   * Subtracts another grid point from this one. If either the resulting {@code row} or {@code col} would be negative,
   * it is set to zero.
   *
   * @param other the point to subtract from this one
   */
  public GridPoint subtract(GridPoint other) {
    return new GridPoint(Math.max(0, this.col - other.col), Math.max(0, this.row - other.row));
  }

  /**
   * Adds another grid point to this one and returns the result.
   *
   * @param other the point to add to this one
   */
  public GridPoint add(GridPoint other) {
    return new GridPoint(this.col + other.col, this.row + other.row);
  }

  /**
   * Adds a column and a row delta to this point and returns the result.
   *
   * @param columnDelta the change in the column index
   * @param rowDelta    the change in the row index
   */
  public GridPoint add(int columnDelta, int rowDelta) {
    return new GridPoint(this.col + columnDelta, this.row + rowDelta);
  }

  public int getCol() {
    return col;
  }

  public int getRow() {
    return row;
  }

  @Override
  public String toString() {
    return String.format("GridPoint(%d, %d)", col, row);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    GridPoint that = (GridPoint) obj;
    return this.col == that.col
        && this.row == that.row;
  }

  @Override
  public int hashCode() {
    return Objects.hash(col, row);
  }
}
