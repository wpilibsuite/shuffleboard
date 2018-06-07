package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.TileLayout;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.scene.layout.GridPane;

/**
 * Helper class for shrinking and moving tiles around to attempt to keep them all in the bounds of their widget pane.
 */
public class TileMover {

  private final WidgetPane pane;

  public TileMover(WidgetPane pane) {
    this.pane = pane;
  }

  public enum Direction {
    HORIZONTAL {
      @Override
      int importantDim(TileSize size) {
        return size.getWidth();
      }
    },
    VERTICAL {
      @Override
      int importantDim(TileSize size) {
        return size.getHeight();
      }
    };

    abstract int importantDim(TileSize size);
  }

  private static TileSize shrinkLeft(TileSize size) {
    return new TileSize(Math.max(1, size.getWidth() - 1), size.getHeight());
  }

  private static TileSize shrinkUp(TileSize size) {
    return new TileSize(size.getWidth(), Math.max(1, size.getHeight() - 1));
  }

  private static TileLayout moveLeft(TileLayout layout) {
    return layout.withCol(layout.origin.col - 1);
  }

  private static TileLayout moveUp(TileLayout layout) {
    return layout.withRow(layout.origin.row - 1);
  }

  /**
   * Collapses or moves the given tile in the given direction by up to <tt>count</tt> rows or columns. Tiles will be
   * moved to fill empty spaces, then the rightmost/bottommost tiles will be shrunk until they reach their minimum size,
   * which is content-dependent.
   *
   * @param tile      the tile to adjust
   * @param count     the desired number of rows or columns by which the tile should be adjusted
   * @param direction the direction in which the tile should be shrunk or moved.
   */
  public void collapseTile(Tile tile, int count, Direction direction) {
    for (int i = 0; i < count; i++) {
      Optional<Runnable> move;
      if (direction == Direction.HORIZONTAL) {
        move = collapseTile(tile, TileMover::moveLeft, TileMover::shrinkLeft, direction);
      } else {
        move = collapseTile(tile, TileMover::moveUp, TileMover::shrinkUp, direction);
      }
      if (move.isPresent()) {
        move.get().run();
      } else {
        // Can't move any further, bail
        break;
      }
    }
  }

  /**
   * Creates a {@link Runnable} that will move or shrink the given tile, as well as moving or shrinking any tiles
   * in the way of moving it. If the tile cannot be shrunk or moved, returns an empty Optional.
   *
   * @param tile                 the tile to move
   * @param targetLayoutFunction the function to use to set the origin for the target location
   * @param shrink               the function to use to shrink the tile
   */
  private Optional<Runnable> collapseTile(Tile tile,
                                          Function<TileLayout, TileLayout> targetLayoutFunction,
                                          Function<TileSize, TileSize> shrink,
                                          Direction direction) {
    boolean left = direction == Direction.HORIZONTAL;
    TileSize minSize = pane.round(tile.getContent().getView().getMinWidth(),
        tile.getContent().getView().getMinHeight());
    TileLayout layout = pane.getTileLayout(tile);
    TileLayout targetLayout = targetLayoutFunction.apply(layout);
    int importantDim = direction.importantDim(layout.size);
    int minDim = direction.importantDim(minSize);
    if (!pane.isOverlapping(targetLayout, n -> n == tile) && !targetLayout.origin.equals(layout.origin)) { // NOPMD
      // Great, we can move it
      return Optional.of(() -> {
        GridPane.setColumnIndex(tile, targetLayout.origin.col);
        GridPane.setRowIndex(tile, targetLayout.origin.row);
      });
    } else if (importantDim > minDim) {
      // Shrink the tile
      return Optional.of(() -> tile.setSize(shrink.apply(tile.getSize())));
    } else if (!targetLayout.origin.equals(layout.origin)) { // NOPMD
      // Try to move or shrink other tiles in the way, then move this one into the free space
      int lower = left ? layout.origin.row : layout.origin.col;
      int upper = lower + importantDim;
      List<Optional<Runnable>> runs = IntStream.range(lower, upper)
          .mapToObj(i -> left ? pane.tileAt(targetLayout.origin.col, i) : pane.tileAt(i, targetLayout.origin.row))
          .flatMap(TypeUtils.optionalStream()) // guaranteed to be at least one tile
          .distinct() // need to make sure we have no repeats, or n-row tiles will get moved n times
          .filter(t -> tile != t)
          .map(t -> collapseTile(t, targetLayoutFunction, shrink, direction)) // recursion here
          .collect(Collectors.toList());
      if (runs.stream().allMatch(Optional::isPresent)) {
        return Optional.of(() -> {
          runs.forEach(r -> r.get().run());
          GridPane.setColumnIndex(tile, targetLayout.origin.col);
          GridPane.setRowIndex(tile, targetLayout.origin.row);
        });
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

}
