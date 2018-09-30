package edu.wpi.first.shuffleboard.app.dnd;

import edu.wpi.first.shuffleboard.api.dnd.DataFormats;

import java.util.Objects;
import java.util.function.Predicate;

import javafx.scene.Node;
import javafx.scene.input.Clipboard;

/**
 * A utility class for helping with drag and drop.
 */
public final class DragUtils {

  /**
   * A predicate for testing if a node is a widget tile being dragged.
   */
  @SuppressWarnings("PMD.LinguisticNaming") // Predicates prefixed with "is" makes PMD mad
  public static final Predicate<Node> isDraggedWidget = DragUtils::isDraggedWidget;

  private DragUtils() {
  }

  /**
   * Checks if a node is a widget tile being dragged.
   *
   * @param node the node to check
   */
  public static boolean isDraggedWidget(Node node) {
    return node != null
        && Clipboard.getSystemClipboard().hasContent(DataFormats.singleTile)
        && Objects.equals(node.getId(),
        ((DataFormats.TileData) Clipboard.getSystemClipboard().getContent(DataFormats.singleTile)).getId());
  }

}
