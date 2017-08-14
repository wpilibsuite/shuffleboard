package edu.wpi.first.shuffleboard.api.widget;

import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;

public interface Viewable {
  Pane getView();

  /**
   * Gets the name to label this item with
   */
  Property<String> nameProperty();

  /**
   * All of the widgets contained by or represented by this one, if any.
   */
  Stream<Widget> allWidgets();
}
