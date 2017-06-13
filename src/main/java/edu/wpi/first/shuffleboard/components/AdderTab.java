package edu.wpi.first.shuffleboard.components;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;

public class AdderTab extends Tab {
  private final Property<Runnable> addTabCallback = new SimpleObjectProperty<>(this, "addTabCallback", () -> {});

  /**
   * Creates a control for adding more tabs to a tab pane.
   */
  public AdderTab() {
    super("+");
    this.setOnSelectionChanged(__event -> {
      getAddTabCallback().run();
      getTabPane().getSelectionModel().selectPrevious();
    });
  }

  public Runnable getAddTabCallback() {
    return addTabCallback.getValue();
  }

  public Property<Runnable> addTabCallbackProperty() {
    return addTabCallback;
  }

  public void setAddTabCallback(Runnable addTabCallback) {
    this.addTabCallback.setValue(addTabCallback);
  }
}
