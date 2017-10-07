package edu.wpi.first.shuffleboard.api.widget;

import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;

/**
 * A Component is base interface for any part of the dashboard that can be instantiated by the user.
 *
 * <p>For example, a static image, a camera widget, and a layout that has children in lists could all be components
 * of different types.
 */
public interface Component {
  Pane getView();

  /**
   * Gets the label for this component.
   */
  Property<String> titleProperty();

  default String getTitle() {
    return titleProperty().getValue();
  }

  default void setTitle(String title) {
    titleProperty().setValue(title);
  }

  /**
   * All of the widgets contained by or represented by this one, if any.
   */
  Stream<Widget> allWidgets();

  /**
   * Gets the name of this widget type.
   */
  String getName();
}
