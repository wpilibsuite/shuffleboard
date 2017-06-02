package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;

/**
 * Represents a tile containing a widget.
 */
public class WidgetTile extends StackPane {

  private final Property<Widget> widget = new SimpleObjectProperty<>(this, "widget", null);
  private final Property<TileSize> size = new SimpleObjectProperty<>(this, "size", null);

  /**
   * Creates an empty tile. The widget and size must be set with {@link #setWidget(Widget)} and
   * {@link #setSize(TileSize)}.
   */
  public WidgetTile() {
    getStyleClass().add("tile");
    widget.addListener((__, oldWidget, newWidget) -> {
      if (oldWidget != null) {
        getChildren().remove(oldWidget.getView());
      }
      if (newWidget != null) {
        getChildren().add(newWidget.getView());
      }
    });
    widget.addListener(__ -> setId("widget-tile[" + getWidget().toString() + "]"));
  }

  /**
   * Creates a tile with the given widget and size.
   */
  public WidgetTile(Widget widget, TileSize size) {
    this();
    setWidget(widget);
    setSize(size);
  }

  public final Widget getWidget() {
    return widget.getValue();
  }

  public final Property<Widget> widgetProperty() {
    return widget;
  }

  /**
   * Sets the widget for this tile. This tile will update to show the view fo the given widget;
   * however, the tile will not change size. The size must be set separately with
   * {@link #setSize(TileSize)}.
   */
  public final void setWidget(Widget widget) {
    this.widget.setValue(widget);
  }

  public final TileSize getSize() {
    return size.getValue();
  }

  public final Property<TileSize> sizeProperty() {
    return size;
  }

  /**
   * Sets the size of this tile. This does not directly change the size of the tile; it is up to
   * the parent pane to actually resize this tile.
   */
  public final void setSize(TileSize size) {
    this.size.setValue(size);
  }

}
