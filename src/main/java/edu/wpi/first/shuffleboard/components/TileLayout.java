package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.widget.TileSize;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class TileLayout {

  public final GridPoint origin;
  public final TileSize size;

  public TileLayout(GridPoint origin, TileSize size) {
    this.origin = requireNonNull(origin, "origin");
    this.size = requireNonNull(size, "size");
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
