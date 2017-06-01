package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.widget.PrefsEditors;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.Optional;

/**
 * Represents a tile containing a widget.
 */
public class WidgetTile extends BorderPane {

  private final Label titleLabel = new Label();
  private final Property<Widget<?>> widget = new SimpleObjectProperty<>(this, "widget", null);
  private final Property<TileSize> size = new SimpleObjectProperty<>(this, "size", null);

  private Pane prefsEditor = null;

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
          setCenter(prefsEditor);
        } else {
          // hide prefs and fire all changes, then switch to widget view
          prefsEditor.getChildren().stream()
                     .filter(n -> n instanceof Control)
                     .map(n -> (Control) n)
                     .forEach(c -> c.fireEvent(new ActionEvent(this, c)));
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
    widget.addListener(__ -> setId("widget-tile[" + getWidget().toString() + "]"));
    widget.addListener(__ -> titleLabel.setText(getWidget().getSourceName()));

    widget.addListener(__ -> prefsEditor = createPrefsController(getWidget().getProperties()));
  }

  @SuppressWarnings("unchecked")
  private Pane createPrefsController(ObservableList<Property<?>> properties) {
    GridPane pane = new GridPane();
    pane.getStyleClass().add("tile-preferences-pane");
    pane.setVgap(8);
    pane.setHgap(8);
    pane.setPadding(new Insets(8));
    int row = 0;
    for (Property property : properties) {
      Optional<Control> editor =
          PrefsEditors.createEditorFor(property.getValue().getClass(), property);
      if (editor.isPresent()) {
        Label label = new Label(property.getName());
        Control control = editor.get();
        pane.add(label, 0, row);
        pane.add(control, 1, row);
        row++;
      } else {
        // skip
        System.out.println("Cannot create editor for " + property);
      }
    }
    pane.setMaxWidth(65535);
    pane.setAlignment(Pos.CENTER);
    return pane;
  }

  /**
   * Creates a tile with the given widget and size.
   */
  public WidgetTile(Widget<?> widget, TileSize size) {
    this();
    setWidget(widget);
    setSize(size);
  }

  public final Widget<?> getWidget() {
    return widget.getValue();
  }

  public final Property<Widget<?>> widgetProperty() {
    return widget;
  }

  /**
   * Sets the widget for this tile. This tile will update to show the view fo the given widget;
   * however, the tile will not change size. The size must be set separately with
   * {@link #setSize(TileSize)}.
   */
  public final void setWidget(Widget<?> widget) {
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
