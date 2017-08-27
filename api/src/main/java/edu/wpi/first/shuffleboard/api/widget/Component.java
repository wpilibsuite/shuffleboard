package edu.wpi.first.shuffleboard.api.widget;

import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.scene.layout.Pane;

public interface Component {
  Pane getView();

  /**
   * Gets the label for this component.
   */
  Property<String> titleProperty();

  /**
   * All of the widgets contained by or represented by this one, if any.
   */
  Stream<Widget> allWidgets();

  /**
   * Gets the name of this widget type.
   */
  String getName();
}
