package edu.wpi.first.shuffleboard.app.dnd;

import edu.wpi.first.shuffleboard.app.components.Tile;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import javafx.scene.Node;

/**
 * A utility class for dealing with resizing tiles.
 */
public final class ResizeUtils {

  @SuppressWarnings("PMD.LinguisticNaming") // Predicates prefixed with "is" makes PMD mad
  public static final Predicate<Node> isResizedTile = n -> n instanceof Tile && isResizedTile((Tile) n);

  private static final AtomicReference<Tile<?>> currentTile = new AtomicReference<>(null);

  private ResizeUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Checks if a tile is currently being resized by a user.
   */
  public static boolean isResizedTile(Tile<?> tile) {
    return currentTile.get() == tile;
  }

  /**
   * Sets the currently resized tile. May be <tt>null</tt> if no tile is being resized.
   */
  public static void setCurrentTile(Tile<?> tile) {
    currentTile.set(tile);
  }

}
