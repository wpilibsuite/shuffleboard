package edu.wpi.first.shuffleboard.components;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;

public class AdderTab extends Tab implements HandledTab {
  private final Property<Runnable> addTabCallback = new SimpleObjectProperty<>(this, "addTabCallback", () -> {});
  private final StringProperty title = new SimpleStringProperty("+");

  /**
   * Creates a control for adding more tabs to a tab pane.
   */
  public AdderTab() {
    super();
    this.setGraphic(new TabHandle(this));
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

  @Override
  public StringProperty titleProperty() {
    return title;
  }

  @Override
  public Tab getTab() {
    return this;
  }

  @Override
  public boolean canEditTitle() {
    return false;
  }
}
