package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.widget.WidgetType;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Collection;

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
  public void loadWidgets(Collection<WidgetType> widgets) {
    widgets.stream().map(WidgetType::get)
            .peek(widget ->
              DummySource.forTypes(widget.getDataTypes())
                         .ifPresent(widget::setSource)
            )
            .forEach(this::addWidget);
  }

  private void addWidget(Widget widget) {
    WidgetGalleryItem item = new WidgetGalleryItem();
    item.setWidget(widget);
    this.getChildren().add(item);
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
