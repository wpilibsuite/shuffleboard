package edu.wpi.first.shuffleboard.dnd;

import javafx.scene.Node;
import javafx.scene.input.Clipboard;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A utility class for helping with drag and drop.
 */
public final class DragUtils {

  /**
   * A predicate for testing if a node is a widget tile being dragged.
   */
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
        && Objects.equals(node.getId(),
                          Clipboard.getSystemClipboard().getContent(DataFormats.widgetTile));
  }

}
