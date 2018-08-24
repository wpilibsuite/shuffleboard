package edu.wpi.first.shuffleboard.app;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.scene.Cursor;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Helper class for resizing the left drawer.
 */
final class DrawerResizer {

  private final Region drawer;
  private final Region handle;

  private boolean didDragInit;
  private boolean dragging;

  private static Map<Region, DrawerResizer> resizers = new WeakHashMap<>();

  /**
   * Attaches a resizer to a drawer.
   *
   * @param drawer the drawer to attach a resizer to
   * @param handle the drag handle node
   */
  public static void attach(Region drawer, Region handle) {
    resizers.computeIfAbsent(drawer, __ -> new DrawerResizer(drawer, handle));
  }

  /**
   * Creates a new drawer resizer.
   *
   * @param drawer the drawer node
   * @param handle the handle node for attaching the drag listeners to
   */
  private DrawerResizer(Region drawer, Region handle) {
    this.drawer = drawer;
    this.handle = handle;

    handle.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
    handle.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
    handle.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
    handle.addEventHandler(DragEvent.DRAG_DONE, __ -> reset());
  }

  private void mousePressed(MouseEvent event) {
    // ignore clicks outside of the draggable margin
    if (!isInDraggableZone(event)) {
      return;
    }

    dragging = true;

    if (!didDragInit) {
      didDragInit = true;
    }
  }


  private void mouseDragged(MouseEvent event) {
    if (!dragging) {
      return;
    }
    handle.setCursor(Cursor.H_RESIZE);

    double newWidth = Math.max(0, Math.min(LeftDrawerController.MAX_WIDTH, event.getSceneX()));
    drawer.setMinWidth(newWidth);
    drawer.setMaxWidth(newWidth);
  }


  private void mouseReleased(MouseEvent event) { // NOPMD unused parameter
    if (!dragging) {
      return;
    }
    dragging = false;
    if (drawer.getWidth() > handle.getWidth()) {
      drawer.getProperties().put(LeftDrawerController.EXPANDED_SIZE_KEY, drawer.getWidth());
    }
  }

  private void reset() {
    didDragInit = false;
    dragging = false;
  }

  private boolean isInDraggableZone(MouseEvent event) {
    return handle.getLayoutBounds().contains(event.getX(), event.getY());
  }
}
