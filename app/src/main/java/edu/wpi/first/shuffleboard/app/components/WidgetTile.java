package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.util.PropertyUtils;
import edu.wpi.first.shuffleboard.app.util.PseudoClassProperty;
import edu.wpi.first.shuffleboard.app.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

/**
 * Represents a tile containing a widget.
 */
public class WidgetTile extends BorderPane {

  private final Property<Widget> widget = new SimpleObjectProperty<>(this, "widget", null);
  private final Property<TileSize> size = new SimpleObjectProperty<>(this, "size", null);
  private final BooleanProperty showWidget = new SimpleBooleanProperty(this, "showWidget", true);

  private final BooleanProperty selected = new PseudoClassProperty(this, "selected");

  /**
   * Creates an empty tile. The widget and size must be set with {@link #setWidget(Widget)} and
   * {@link #setSize(TileSize)}.
   */
  public WidgetTile() {
    try {
      FXMLLoader loader = new FXMLLoader(WidgetTile.class.getResource("WidgetTile.fxml"));
      loader.setRoot(this);
      loader.load();
      getStyleClass().addAll("tile", "card");
      PropertyUtils.bindWithConverter(idProperty(), widget, w -> "widget-tile[" + w + "]");
    } catch (IOException e) {
      throw new RuntimeException("Could not load the widget tile FXML", e);
    }
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

  public boolean isShowWidget() {
    return showWidget.get();
  }

  public BooleanProperty showWidgetProperty() {
    return showWidget;
  }

  public void setShowWidget(boolean showWidget) {
    this.showWidget.set(showWidget);
  }

  public void toggleShowWidget() {
    setShowWidget(!isShowWidget());
  }

  public boolean isSelected() {
    return selected.get();
  }

  public void setSelected(boolean value) {
    selected.set(value);
  }

  public BooleanProperty selectedProperty() {
    return selected;
  }
}
