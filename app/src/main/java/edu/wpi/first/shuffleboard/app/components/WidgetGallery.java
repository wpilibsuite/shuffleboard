package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;

import java.io.IOException;
import java.util.Collection;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class WidgetGallery extends TilePane {
  /**
   * Creates a new WidgetGallery. This loads WidgetGallery.fxml and set up the constructor
   */
  public WidgetGallery() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WidgetGallery.fxml"));
    fxmlLoader.setRoot(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new IllegalStateException("Can't load FXML : " + getClass().getSimpleName(), e);
    }
  }

  /**
   * Add the given widget types to the gallery.
   */
  public void setWidgets(Collection<WidgetType> widgets) {
    clear();
    widgets.stream()
            .map(WidgetType::get)
            .flatMap(TypeUtils.castStream(Widget.class))
            .peek(widget ->
              DummySource.forTypes(widget.getDataTypes())
                         .ifPresent(widget::addSource)
            )
            .forEach(this::addWidget);
  }

  private void addWidget(Widget widget) {
    WidgetGalleryItem item = new WidgetGalleryItem();
    item.setWidget(widget);
    this.getChildren().add(item);
  }

  public void clear() {
    getChildren().clear();
  }

  public static class WidgetGalleryItem extends VBox {

    private final Property<Widget> widget = new SimpleObjectProperty<>(this, "widget", null);

    private WidgetGalleryItem() {
      this.getStyleClass().add("item");
      this.widget.addListener((property, oldValue, newWidget) -> {
        this.getChildren().clear();
        if (newWidget != null) {
          StackPane dragTarget = new StackPane();
          dragTarget.getStyleClass().add("tile");
          dragTarget.getChildren().add(newWidget.getView());
          dragTarget.getChildren().add(new Pane());
          dragTarget.setMaxSize(128, 128);

          this.getChildren().add(dragTarget);
          setVgrow(dragTarget, Priority.ALWAYS);
          this.getChildren().add(new Label(newWidget.getName()));
        }
      });
    }

    public void setWidget(Widget widget) {
      this.widget.setValue(widget);
    }

    public Property<Widget> getWidgetProperty() {
      return widget;
    }

    public Widget getWidget() {
      return this.widget.getValue();
    }

  }
}
