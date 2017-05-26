package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.scene.Node;

import java.util.UUID;

/**
 * Allows us to keep track of widgets and their UI elements in the view.
 */
public final class WidgetHandle {

  /**
   * A unique string used to identify this handle.
   */
  private final String id = UUID.randomUUID().toString();
  private final Widget widget;
  private TileSize currentSize;
  private String sourceName;
  private Node uiElement;

  /**
   * Creates a handle for the given widget.
   */
  public WidgetHandle(Widget widget) {
    this.widget = widget;
    sourceName = widget.getSourceName();
  }

  /**
   * Gets the unique string identifying this handle.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the widget being handled.
   */
  public Widget getWidget() {
    return widget;
  }

  /**
   * Gets the current tile size of the widget's UI element.
   */
  public TileSize getCurrentSize() {
    return currentSize;
  }

  public void setCurrentSize(TileSize currentSize) {
    this.currentSize = currentSize;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public Node getUiElement() {
    return uiElement;
  }

  public void setUiElement(Node uiElement) {
    this.uiElement = uiElement;
  }
}
