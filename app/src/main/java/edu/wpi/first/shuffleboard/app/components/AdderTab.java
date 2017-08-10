package edu.wpi.first.shuffleboard.app.components;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;

public class AdderTab extends Tab implements HandledTab {
  private final Property<Runnable> addTabCallback = new SimpleObjectProperty<>(this, "addTabCallback", () -> {});
  private final StringProperty title = new SimpleStringProperty("+");

  /**
   * Creates a control for adding more tabs to a tab pane.
   */
  public AdderTab() {
    super();
    TabHandle handle = new TabHandle(this);
    this.setGraphic(handle);

    this.setOnSelectionChanged(__event -> {
      getTabPane().getSelectionModel().selectFirst();
      if (getTabPane().getSelectionModel().getSelectedItem().equals(this)) {
        addTabAndFocus();
      }
    });

    handle.addEventHandler(MouseEvent.MOUSE_PRESSED, me -> {
      addTabAndFocus();
      me.consume();
    });
  }

  public void addTabAndFocus() {
    getAddTabCallback().run();
    getTabPane().getSelectionModel().select(getTabPane().getTabs().size() - 2);
  }

  @Override
  public void onDragOver() {
    addTabAndFocus();
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
