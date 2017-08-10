package edu.wpi.first.shuffleboard.app.components;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;

public interface HandledTab {
  StringProperty titleProperty();

  /**
   * Tab isn't an interface, so we have to do this kinda clunky workaround.
   */
  Tab getTab();

  default void onDragOver() {
    getTab().getTabPane().getSelectionModel().select(getTab());
  }

  default boolean canEditTitle() {
    return true;
  }
}
