package edu.wpi.first.shuffleboard.api.widget;

import javafx.scene.layout.Pane;

public interface Viewable {
  Pane getView();

  /**
   * Gets the name to label this item with
   */
  default String getName() {
    return "";
  }
}
