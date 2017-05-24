package edu.wpi.first.shuffleboard;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents a point in a grid.
 */
class Point {

  public final int col, row;

  Point(int col, int row) {
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
