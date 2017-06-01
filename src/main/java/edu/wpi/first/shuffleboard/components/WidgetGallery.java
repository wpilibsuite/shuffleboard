package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.WidgetType;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Collection;

public class WidgetGallery extends TilePane {
  public WidgetGallery() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/edu/wpi/first/shuffleboard/components/WidgetGallery.fxml"));
    fxmlLoader.setRoot(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new IllegalStateException("Can't load FXML : " + getClass().getSimpleName(), e);
    }
  }

  public void loadWidgets(Collection<WidgetType> widgets) {
    widgets.stream().map(WidgetType::get)
            .peek(widget -> {
              DummySource.forTypes(widget.getDataTypes())
                         .ifPresent(widget::setSource);
            })
            .forEach(this::addWidget);
  }

  private void addWidget(Widget widget) {
    WidgetGalleryItem item = new WidgetGalleryItem();
    item.setWidget(widget);
    this.getChildren().add(item);
  }

  public class WidgetGalleryItem extends VBox {

    private Property<Widget> widget = new SimpleObjectProperty<>(this, "widget", null);

    private WidgetGalleryItem() {
      this.getStyleClass().add("item");
      this.widget.addListener((property, oldValue, newWidget) -> {
        this.getChildren().clear();
        if (newWidget != null) {
          StackPane dragTarget = new StackPane();
          dragTarget.getStyleClass().add("tile");
          dragTarget.getChildren().add(newWidget.getView());
          dragTarget.getChildren().add(new Pane());

          this.getChildren().add(dragTarget);
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
