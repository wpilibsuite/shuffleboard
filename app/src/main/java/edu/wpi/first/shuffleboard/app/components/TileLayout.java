package edu.wpi.first.shuffleboard.app.components;


import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Represents how a tile is laid out. Contains the origin point as well as the size of the tile.
 */
public final class TileLayout {

  public final GridPoint origin;
  public final TileSize size;

  public TileLayout(GridPoint origin, TileSize size) {
    this.origin = requireNonNull(origin, "origin");
    this.size = requireNonNull(size, "size");
  }

  public TileLayout withCol(int col) {
    return new TileLayout(new GridPoint(Math.max(0, col), origin.row), size);
  }

  public TileLayout withRow(int row) {
    return new TileLayout(new GridPoint(origin.col, Math.max(0, row)), size);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    TileLayout that = (TileLayout) obj;

    return this.origin.equals(that.origin)
        && this.size.equals(that.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(origin, size);
  }

  @Override
  public String toString() {
    return String.format("TileLayout(origin=%s, size=%s)", origin, size);
  }

}
