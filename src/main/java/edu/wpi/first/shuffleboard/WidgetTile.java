package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.PreferencesPane;
import edu.wpi.first.shuffleboard.util.PropertyUtils;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.WidgetPrefsController;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

/**
 * Represents a tile containing a widget.
 */
public class WidgetTile extends BorderPane {

  private final Label titleLabel = new Label();
  private final Property<Widget> widget = new SimpleObjectProperty<>(this, "widget", null);
  private final Property<TileSize> size = new SimpleObjectProperty<>(this, "size", null);
  private final Property<Pane> prefsEditor = new SimpleObjectProperty<>(this, "prefsEditor", null);

  /**
   * Creates an empty tile. The widget and size must be set with {@link #setWidget(Widget)} and
   * {@link #setSize(TileSize)}.
   */
  public WidgetTile() {
    getStyleClass().add("tile");
    Pane titleBar = new StackPane(titleLabel);
    titleBar.getStyleClass().add("tile-title-bar");
    titleLabel.getStyleClass().add("tile-title-label");

    titleLabel.setAlignment(Pos.CENTER);
    titleLabel.setMaxWidth(Double.POSITIVE_INFINITY);
    setTop(titleBar);
    addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      if (event.getClickCount() == 2 && !getWidget().getProperties().isEmpty()) {
        if (getCenter() == getWidget().getView()) {
          // show prefs
          setCenter(prefsEditor.getValue());
          prefsEditor.getValue().setManaged(true);
        } else {
          prefsEditor.getValue().setManaged(false);
          setCenter(getWidget().getView());
        }
      }
    });
    focusedProperty().addListener((obs, wasFocus, isFocus) -> {
      if (!isFocus) {
        // reset on focus lost
        setCenter(getWidget().getView());
      }
    });
    widget.addListener((__, oldWidget, newWidget) -> setCenter(newWidget.getView()));
    PropertyUtils.bind(idProperty(), widget, w -> "widget-tile[" + w.toString() + "]");
    PropertyUtils.bind(titleLabel.textProperty(), widget, w -> w.getSource().getName());
    PropertyUtils.bind(prefsEditor, widget, w -> createPrefsController(w.getProperties()));
  }

  private Pane createPrefsController(List<Property<?>> properties) {
    try {
      PreferencesPane preferencesPane
          = FXMLLoader.load(WidgetPrefsController.class.getResource("WidgetPrefs.fxml"));
      preferencesPane.populateFrom(properties);
      return preferencesPane;
    } catch (IOException e) {
      throw new RuntimeException("Could not load the widget preferences FXML", e);
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

}
