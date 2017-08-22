package edu.wpi.first.shuffleboard.api.widget;

import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;

public interface Component {
  Pane getView();

  /**
   * Gets the name to label this item with
   */
  Property<String> nameProperty();

  /**
   * All of the widgets contained by or represented by this one, if any.
   */
  Stream<Widget> allWidgets();

  /**
   * The unique name of this component.
   */
  String getName();
}
