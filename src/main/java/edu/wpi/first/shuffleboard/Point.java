package edu.wpi.first.shuffleboard;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a point in a grid.
 */
public class Point {

  public final int col;
  public final int row;

  /**
   * Creates a point at the given column and row indices.
   */
  public Point(int col, int row) {
    checkArgument(col >= 0, "Column index must be non-negative, was " + col);
    checkArgument(row >= 0, "Row index must be non-negative, was " + row);
    this.col = col;
    this.row = row;
  }

  public int getCol() {
    return col;
  }

  public int getRow() {
    return row;
  }

}
